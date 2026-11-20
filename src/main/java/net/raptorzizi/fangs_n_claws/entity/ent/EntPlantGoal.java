package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EntPlantGoal extends Goal {

    // Search
    private static final int SEARCH_RADIUS = 10;
    private static final int SEARCH_HEIGHT = 4;
    private static final int SEARCH_TRIES = 48;
    private static final int HEADROOM = 4;
    private static final int TRUNK_CLEARANCE = 2;

    // Pace
    private static final double PLANT_DISTANCE = 2.5;
    private static final double WALK_SPEED = 0.9;
    private static final int GIVEN_COOLDOWN = 40;
    private static final int RETRY_COOLDOWN = 100;
    private static final int GIVE_UP_TICKS = 200;
    private static final int PLANT_HIT_TICK = 13;
    private static final int PLANT_TOTAL_TICKS = 25;

    private final EntEntity ent;
    private BlockPos spot;
    private Block plant;
    private boolean fromInventory;
    private int cooldown;
    private int ticks;
    private int plantTick;
    private boolean planted;

    public EntPlantGoal(EntEntity ent) {
        this.ent = ent;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (this.ent.isDormant() || this.ent.getTarget() != null || this.ent.isAttacking()) return false;
        if (!EventHooks.canEntityGrief(this.ent.level(), this.ent)) return false;

        this.fromInventory = this.ent.hasPlantables();
        if (!this.fromInventory && !this.ent.canSelfPlant()) return false;

        this.plant = this.fromInventory ? this.inventoryPlant() : this.biomePlant();
        if (this.plant == null) {
            this.cooldown = RETRY_COOLDOWN;
            return false;
        }

        this.spot = this.findSpot();
        if (this.spot == null) {
            this.cooldown = RETRY_COOLDOWN;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.spot != null && this.ticks < GIVE_UP_TICKS
                && !this.ent.isDormant() && this.ent.getTarget() == null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.plantTick = 0;
        this.planted = false;
        this.moveToSpot();
    }

    @Override
    public void stop() {
        if (this.spot != null && !this.planted) this.cooldown = RETRY_COOLDOWN;
        this.spot = null;
        this.plantTick = 0;
        this.ent.getNavigation().stop();
    }

    /**
     * Le selecteur ne repasse par canContinueToUse qu'un tick sur deux et appelle tick entre les
     * deux : une fois la plante posee, spot est nul jusqu'a l'arret du goal.
     */
    @Override
    public void tick() {
        if (this.spot == null) return;

        this.ticks++;
        this.ent.keepAwake();
        this.ent.getLookControl().setLookAt(this.spot.getX() + 0.5, this.spot.getY(), this.spot.getZ() + 0.5);

        double dx = this.ent.getX() - (this.spot.getX() + 0.5);
        double dz = this.ent.getZ() - (this.spot.getZ() + 0.5);
        if (this.plantTick == 0 && dx * dx + dz * dz > PLANT_DISTANCE * PLANT_DISTANCE) {
            if (this.ticks % 20 == 0 || this.ent.getNavigation().isDone()) this.moveToSpot();
            return;
        }

        this.tickPlanting();
    }

    /**
     * Arrive a la parcelle, l'ent s'arrete et joue l'animation plant : la plante apparait quand sa
     * main touche le sol, au tick PLANT_HIT_TICK, et il repart a la fin de l'animation.
     */
    private void tickPlanting() {
        this.ent.getNavigation().stop();
        if (this.plantTick == 0) this.ent.playPlantAnimation();
        this.plantTick++;

        if (this.plantTick == PLANT_HIT_TICK && this.isValidSpot(this.spot)) {
            this.place();
            this.planted = true;
        }
        if (this.plantTick >= PLANT_TOTAL_TICKS) this.spot = null;
    }

    private void moveToSpot() {
        this.ent.getNavigation().moveTo(this.spot.getX() + 0.5, this.spot.getY(), this.spot.getZ() + 0.5, WALK_SPEED);
    }

    // Choice

    private boolean isSingleCell() {
        BlockState state = this.plant.defaultBlockState();
        return state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.TALL_FLOWERS)
                || EntBiomePlants.isMushroom(this.plant);
    }

    private boolean isTall() {
        return this.plant instanceof DoublePlantBlock;
    }

    @Nullable
    private Block inventoryPlant() {
        SimpleContainer inventory = this.ent.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (EntEntity.isPlantable(stack) && stack.getItem() instanceof BlockItem blockItem) return blockItem.getBlock();
        }
        return null;
    }

    /**
     * Sans pousse ni fleur donnee, l'ent ne plante que ce que le biome ou il se tient fait pousser :
     * dans un biome qui n'a ni arbre, ni fleur, ni champignon, il ne plante rien.
     */
    @Nullable
    private Block biomePlant() {
        Level level = this.ent.level();
        return EntBiomePlants.of(level.getBiome(this.ent.blockPosition())).pick(this.ent.getRandom());
    }

    // Spot

    @Nullable
    private BlockPos findSpot() {
        Level level = this.ent.level();
        RandomSource random = this.ent.getRandom();
        BlockPos origin = this.ent.blockPosition();

        for (int attempt = 0; attempt < SEARCH_TRIES; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            for (int y = origin.getY() + SEARCH_HEIGHT; y >= origin.getY() - SEARCH_HEIGHT; y--) {
                BlockPos at = new BlockPos(x, y, z);
                if (!level.getBlockState(at.below()).is(BlockTags.DIRT) || !this.isFree(at)) continue;
                if (this.isValidSpot(at)) return at;
                break;
            }
        }
        return null;
    }

    private boolean isFree(BlockPos at) {
        BlockState state = this.ent.level().getBlockState(at);
        return state.canBeReplaced() && state.getFluidState().isEmpty()
                && !state.is(BlockTags.SAPLINGS) && !state.is(BlockTags.FLOWERS);
    }

    private boolean isValidSpot(BlockPos at) {
        return this.isSingleCell() ? this.isCellSpot(at) : this.isOpenPlot(at);
    }

    /**
     * Une fleur ou un champignon : une case libre ou il tient. canSurvive fait le tri pour les
     * champignons, qui veulent de l'ombre hors du mycelium et du podzol.
     */
    private boolean isCellSpot(BlockPos at) {
        if (!this.isFree(at)) return false;
        if (this.isTall() && !this.isFree(at.above())) return false;
        return this.plant.defaultBlockState().canSurvive(this.ent.level(), at);
    }

    /**
     * La parcelle d'une pousse : 3 par 3 cases libres sur de la terre, quatre blocs d'air au-dessus du
     * centre pour que l'arbre ait la place de pousser, et ni tronc ni pousse a moins de
     * TRUNK_CLEARANCE blocs.
     */
    private boolean isOpenPlot(BlockPos center) {
        Level level = this.ent.level();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos cell = center.offset(dx, 0, dz);
                if (!this.isFree(cell) || !level.getBlockState(cell.below()).is(BlockTags.DIRT)) return false;
            }
        }
        for (int dy = 1; dy <= HEADROOM; dy++) {
            if (!level.isEmptyBlock(center.above(dy))) return false;
        }
        for (BlockPos near : BlockPos.betweenClosed(center.offset(-TRUNK_CLEARANCE, 0, -TRUNK_CLEARANCE),
                center.offset(TRUNK_CLEARANCE, 2, TRUNK_CLEARANCE))) {
            BlockState state = level.getBlockState(near);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.SAPLINGS)) return false;
        }
        return this.plant.defaultBlockState().canSurvive(level, center);
    }

    // Planting

    /**
     * Un chene noir ne pousse qu'en carre de quatre : on en plante quatre dans la parcelle, qui a la
     * place, des que l'ent en a assez ou qu'il plante de lui-meme. Une grande fleur pose ses deux
     * moities d'un coup.
     */
    private void place() {
        boolean square = this.plant == Blocks.DARK_OAK_SAPLING
                && (!this.fromInventory || this.ent.getInventory().countItem(this.plant.asItem()) >= 4);
        List<BlockPos> cells = new ArrayList<>(List.of(this.spot));
        if (square) cells.addAll(List.of(this.spot.east(), this.spot.south(), this.spot.south().east()));

        ServerLevel level = (ServerLevel) this.ent.level();
        BlockState state = this.plant.defaultBlockState();
        for (BlockPos cell : cells) {
            if (!this.isFree(cell)) continue;
            if (this.isTall()) {
                DoublePlantBlock.placeAt(level, state, cell, Block.UPDATE_ALL);
            } else {
                level.setBlock(cell, state, Block.UPDATE_ALL);
            }
            level.gameEvent(GameEvent.BLOCK_PLACE, cell, GameEvent.Context.of(this.ent, state));
            if (this.fromInventory) this.consumeOne();
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, cell.getX() + 0.5, cell.getY() + 0.3, cell.getZ() + 0.5,
                    6, 0.3, 0.2, 0.3, 0.0);
        }
        this.ent.playSound(SoundEvents.GRASS_PLACE, 1.0F, 0.8F);

        if (this.fromInventory) {
            this.cooldown = GIVEN_COOLDOWN;
        } else {
            this.ent.onSelfPlanted();
        }
    }

    private void consumeOne() {
        SimpleContainer inventory = this.ent.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(this.plant.asItem())) {
                stack.shrink(1);
                inventory.setChanged();
                return;
            }
        }
    }
}
