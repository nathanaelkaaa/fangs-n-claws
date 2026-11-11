package net.raptorzizi.fangs_n_claws.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stalactite et stalagmite de vermicite, calquees sur le pointed dripstone mais sans sa
 * croissance, son ruissellement ni son remplissage de chaudron : c'est une pierre morte.
 * La logique vanilla est recopiee plutot qu'heritee, parce que ses aides sont statiques et
 * testent {@code Blocks.POINTED_DRIPSTONE} en dur — nos pointes ne s'epaissiraient jamais
 * entre elles.
 */
public class PointedVermiciteBlock extends Block implements Fallable, SimpleWaterloggedBlock {

    public static final MapCodec<PointedVermiciteBlock> CODEC = simpleCodec(PointedVermiciteBlock::new);

    public static final DirectionProperty                TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS     = BlockStateProperties.DRIPSTONE_THICKNESS;
    public static final BooleanProperty                  WATERLOGGED   = BlockStateProperties.WATERLOGGED;

    // Constants

    private static final int   DELAY_BEFORE_FALLING  = 2;
    private static final float MAX_HORIZONTAL_OFFSET = 0.125F;

    /** Degats d'une stalactite qui tombe : 1 par bloc de hauteur, plafonne a 40. */
    private static final float STALACTITE_DAMAGE_PER_BLOCK = 1.0F;
    private static final int   STALACTITE_MAX_DAMAGE       = 40;
    private static final int   STALACTITE_MIN_HEIGHT       = 6;

    private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0F;
    private static final float STALAGMITE_FALL_DAMAGE_FACTOR   = 2.0F;

    private static final double MIN_TRIDENT_VELOCITY_TO_BREAK = 0.6;

    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP    = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN  = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE   = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape MIDDLE_SHAPE    = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape BASE_SHAPE      = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    // Setup

    public PointedVermiciteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TIP_DIRECTION, Direction.UP)
                .setValue(THICKNESS,     DripstoneThickness.TIP)
                .setValue(WATERLOGGED,   false));
    }

    @Override
    protected @NotNull MapCodec<PointedVermiciteBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
    }

    // Placement

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction looking = context.getNearestLookingVerticalDirection().getOpposite();
        Direction tip = calculateTipDirection(level, pos, looking);
        if (tip == null) return null;

        boolean allowMerge = !context.isSecondaryUseActive();
        DripstoneThickness thickness = calculateThickness(level, pos, tip, allowMerge);
        if (thickness == null) return null;

        return this.defaultBlockState()
                .setValue(TIP_DIRECTION, tip)
                .setValue(THICKNESS,     thickness)
                .setValue(WATERLOGGED,   level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return isValidPlacement(level, pos, state.getValue(TIP_DIRECTION));
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction side,
                                              @NotNull BlockState neighbour, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (side != Direction.UP && side != Direction.DOWN) return state;

        Direction tip = state.getValue(TIP_DIRECTION);
        if (tip == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) return state;

        if (side == tip.getOpposite() && !this.canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, tip == Direction.DOWN ? DELAY_BEFORE_FALLING : 1);
            return state;
        }

        boolean merged = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
        return state.setValue(THICKNESS, calculateThickness(level, pos, tip, merged));
    }

    // Shape

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = switch (state.getValue(THICKNESS)) {
            case TIP_MERGE -> TIP_MERGE_SHAPE;
            case TIP       -> state.getValue(TIP_DIRECTION) == Direction.DOWN ? TIP_SHAPE_DOWN : TIP_SHAPE_UP;
            case FRUSTUM   -> FRUSTUM_SHAPE;
            case MIDDLE    -> MIDDLE_SHAPE;
            case BASE      -> BASE_SHAPE;
        };

        Vec3 offset = state.getOffset(level, pos);
        return shape.move(offset.x, 0.0, offset.z);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                    @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean isCollisionShapeFullBlock(@NotNull BlockState state, @NotNull BlockGetter level,
                                                @NotNull BlockPos pos) {
        return false;
    }

    @Override
    protected float getMaxHorizontalOffset() {
        return MAX_HORIZONTAL_OFFSET;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    protected @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // Hazard

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos,
                       @NotNull Entity entity, float fallDistance) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP
                && state.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(fallDistance + STALAGMITE_FALL_DISTANCE_OFFSET,
                    STALAGMITE_FALL_DAMAGE_FACTOR, level.damageSources().stalagmite());
            return;
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    protected void onProjectileHit(@NotNull Level level, @NotNull BlockState state,
                                   @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
        if (level.isClientSide()) return;

        BlockPos pos = hit.getBlockPos();
        if (projectile instanceof ThrownTrident
                && projectile.mayInteract(level, pos)
                && projectile.mayBreak(level)
                && projectile.getDeltaMovement().length() > MIN_TRIDENT_VELOCITY_TO_BREAK) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level,
                        @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (isStalagmite(state) && !this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        spawnFallingStalactite(state, level, pos);
    }

    private void spawnFallingStalactite(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        BlockState current = state;

        while (isStalactite(current)) {
            FallingBlockEntity falling = FallingBlockEntity.fall(level, cursor, current);
            if (isTip(current, true)) {
                int height = Math.max(1 + pos.getY() - cursor.getY(), STALACTITE_MIN_HEIGHT);
                falling.setHurtsEntities(STALACTITE_DAMAGE_PER_BLOCK * height, STALACTITE_MAX_DAMAGE);
                return;
            }
            cursor.move(Direction.DOWN);
            current = level.getBlockState(cursor);
        }
    }

    @Override
    public void onBrokenAfterFall(@NotNull Level level, @NotNull BlockPos pos,
                                  @NotNull FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) level.levelEvent(1045, pos, 0);
    }

    @Override
    public @NotNull DamageSource getFallDamageSource(@NotNull Entity entity) {
        return entity.damageSources().fallingStalactite(entity);
    }

    // State helpers

    private boolean isSelfWithDirection(BlockState state, Direction dir) {
        return state.is(this) && state.getValue(TIP_DIRECTION) == dir;
    }

    private boolean isStalactite(BlockState state) {
        return isSelfWithDirection(state, Direction.DOWN);
    }

    private boolean isStalagmite(BlockState state) {
        return isSelfWithDirection(state, Direction.UP);
    }

    private boolean isTip(BlockState state, boolean allowMerge) {
        if (!state.is(this)) return false;
        DripstoneThickness thickness = state.getValue(THICKNESS);
        return thickness == DripstoneThickness.TIP
                || (allowMerge && thickness == DripstoneThickness.TIP_MERGE);
    }

    /** Une pointe tient sur une face solide, ou sur une autre pointe orientee pareil. */
    private boolean isValidPlacement(LevelReader level, BlockPos pos, Direction dir) {
        BlockPos anchorPos = pos.relative(dir.getOpposite());
        BlockState anchor = level.getBlockState(anchorPos);
        return anchor.isFaceSturdy(level, anchorPos, dir) || isSelfWithDirection(anchor, dir);
    }

    @Nullable
    private Direction calculateTipDirection(LevelReader level, BlockPos pos, Direction dir) {
        if (isValidPlacement(level, pos, dir)) return dir;
        if (isValidPlacement(level, pos, dir.getOpposite())) return dir.getOpposite();
        return null;
    }

    private DripstoneThickness calculateThickness(LevelReader level, BlockPos pos,
                                                  Direction dir, boolean allowMerge) {
        Direction back = dir.getOpposite();
        BlockState ahead = level.getBlockState(pos.relative(dir));

        if (isSelfWithDirection(ahead, back)) {
            return !allowMerge && ahead.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE
                    ? DripstoneThickness.TIP
                    : DripstoneThickness.TIP_MERGE;
        }
        if (!isSelfWithDirection(ahead, dir)) return DripstoneThickness.TIP;

        DripstoneThickness aheadThickness = ahead.getValue(THICKNESS);
        if (aheadThickness == DripstoneThickness.TIP || aheadThickness == DripstoneThickness.TIP_MERGE) {
            return DripstoneThickness.FRUSTUM;
        }
        return isSelfWithDirection(level.getBlockState(pos.relative(back)), dir)
                ? DripstoneThickness.MIDDLE
                : DripstoneThickness.BASE;
    }
}
