package net.raptorzizi.fangs_n_claws.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import org.jetbrains.annotations.Nullable;

public class GhostBlock extends BaseEntityBlock {

    public static final MapCodec<GhostBlock> CODEC = simpleCodec(properties -> new GhostBlock());

    public static final BooleanProperty HAS_MIMIC = BooleanProperty.create("has_mimic");

    private static final double SLOWDOWN = 0.4;

    public GhostBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.3f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn(Blocks::never)
                .isViewBlocking((s, g, p) -> false)
                .isRedstoneConductor((s, g, p) -> false)
                .isSuffocating((s, g, p) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_MIMIC, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HAS_MIMIC);
    }

    // Block entity

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GhostBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HAS_MIMIC) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    // Apply texture

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState candidate = blockItem.getBlock().defaultBlockState();
            if (candidate.isSolid() && !(blockItem.getBlock() instanceof GhostBlock)) {
                if (!level.isClientSide) {
                    if (level.getBlockEntity(pos) instanceof GhostBlockEntity be) {
                        be.setMimickedState(candidate);
                        be.setChanged();
                    }
                    if (state.getValue(HAS_MIMIC)) {
                        level.setBlock(pos, state.setValue(HAS_MIMIC, false), 2);
                    }
                    level.setBlock(pos, state.setValue(HAS_MIMIC, true), 3);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (player instanceof ServerPlayer sp) {
                        FncAdvancements.grant(sp, "forge/chameleon");
                    }
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    // Remove texture

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (state.getValue(HAS_MIMIC)) {
            if (!level.isClientSide) {
                if (level.getBlockEntity(pos) instanceof GhostBlockEntity be) {
                    be.setMimickedState(null);
                    be.setChanged();
                }
                level.setBlock(pos, state.setValue(HAS_MIMIC, false), 3);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    // Physics

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.makeStuckInBlock(state, new Vec3(SLOWDOWN, SLOWDOWN, SLOWDOWN));
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ctx && ctx.getEntity() instanceof LivingEntity) {
            return Shapes.empty();
        }
        return state.getShape(level, pos);
    }

    // Rendering helpers

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
