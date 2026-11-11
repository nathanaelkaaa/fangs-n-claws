package net.raptorzizi.fangs_n_claws.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleGrubEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PurpleWormEggBlock extends BaseEntityBlock {

    public static final MapCodec<PurpleWormEggBlock> CODEC = simpleCodec(properties -> new PurpleWormEggBlock());

    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final int MAX_HATCH_LEVEL = 2;

    private static final int HATCH_TIME_TICKS    = 96000;
    private static final int RANDOM_OFFSET_TICKS = 1200;
    private static final double HATCH_RANGE        = 10.0;
    private static final int    OWNER_RETRY_TICKS  = 100;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public PurpleWormEggBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.5F)
                .sound(SoundType.SLIME_BLOCK)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    // Block entity

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PurpleWormEggBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // Ownership

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || placer == null) return;
        if (level.getBlockEntity(pos) instanceof PurpleWormEggBlockEntity egg) {
            egg.setOwner(placer.getUUID());
        }
    }

    @Nullable
    private UUID readOwner(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PurpleWormEggBlockEntity egg ? egg.getOwner() : null;
    }

    private boolean ownerIsNear(Level level, BlockPos pos, @Nullable UUID owner) {
        if (owner == null) return true;

        Player player = level.getPlayerByUUID(owner);
        return player != null && pos.closerToCenterThan(player.position(), HATCH_RANGE);
    }

    // Hatching

    public int getHatchLevel(BlockState state) {
        return state.getValue(HATCH);
    }

    private boolean isReadyToHatch(BlockState state) {
        return this.getHatchLevel(state) == MAX_HATCH_LEVEL;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                           @NotNull BlockState oldState, boolean movedByPiston) {
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(state));
        scheduleNextStage(level, pos);
    }

    private void scheduleNextStage(Level level, BlockPos pos) {
        int perStage = HATCH_TIME_TICKS / (MAX_HATCH_LEVEL + 1);
        level.scheduleTick(pos, this, perStage + level.random.nextInt(RANDOM_OFFSET_TICKS));
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level,
                        @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!this.isReadyToHatch(state)) {
            level.playSound(null, pos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS,
                    0.7F, 0.7F + random.nextFloat() * 0.2F);
            level.setBlock(pos, state.setValue(HATCH, this.getHatchLevel(state) + 1), 2);
            scheduleNextStage(level, pos);
            return;
        }

        UUID owner = this.readOwner(level, pos);
        if (!ownerIsNear(level, pos, owner)) {
            level.scheduleTick(pos, this, OWNER_RETRY_TICKS);
            return;
        }

        level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS,
                0.7F, 0.7F + random.nextFloat() * 0.2F);
        level.destroyBlock(pos, false);

        PurpleGrubEntity grub = EntityRegistry.PURPLE_GRUB.get().create(level);
        if (grub == null) return;

        Vec3 centre = pos.getCenter();
        grub.moveTo(centre.x, centre.y, centre.z,
                Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F);
        grub.setPersistenceRequired();
        grub.setOwnerUUID(owner);
        level.addFreshEntity(grub);
    }
}
