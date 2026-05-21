package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BearTrapBlock extends Block {

    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    private static final ResourceKey<DamageType> BEAR_TRAP_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, FangsClawsMod.id("bear_trap"));

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    private static final double DETECT_HEIGHT = 0.5;
    private static final int TICK_INTERVAL = 5;

    public BearTrapBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(CLOSED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLOSED);
    }

    // Shape

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    // Placement

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.getValue(CLOSED)) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(CLOSED)) return;

        AABB area = new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + DETECT_HEIGHT, pos.getZ() + 1);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> !(e instanceof Player p && p.getAbilities().instabuild));

        if (!targets.isEmpty()) {
            LivingEntity victim = targets.get(0);
            level.setBlock(pos, state.setValue(CLOSED, true), 3);
            DamageSource source = new DamageSource(
                    level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BEAR_TRAP_DAMAGE));
            victim.hurt(source, 8.0f);
            victim.addEffect(new MobEffectInstance(MobEffectsRegistry.BLEEDING.getDelegate(), 200, 1)); // Bleeding 2, 10s
            victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.1, 1.0, 0.1));
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 0.5f);
        } else {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    // Reset

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (state.getValue(CLOSED)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(CLOSED, false), 3);
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN,
                        SoundSource.BLOCKS, 1.0f, 1.2f);
                level.scheduleTick(pos, this, TICK_INTERVAL);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
