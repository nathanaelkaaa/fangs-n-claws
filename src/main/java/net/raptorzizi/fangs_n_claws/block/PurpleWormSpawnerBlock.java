package net.raptorzizi.fangs_n_claws.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PurpleWormSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<PurpleWormSpawnerBlock> CODEC = simpleCodec(PurpleWormSpawnerBlock::new);

    public PurpleWormSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Declenchement manuel en creation : le tick automatique ne regarde que les joueurs en survie,
     * donc sans cela le bloc serait intestable.
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
                                                        @NotNull Level level, @NotNull BlockPos pos,
                                                        Player player, @NotNull BlockHitResult hit) {
        if (!player.isCreative()) return super.useWithoutItem(state, level, pos, player, hit);
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        return PurpleWormSpawnerBlockEntity.spawn(serverLevel, pos)
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }

    // Block entity

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PurpleWormSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
                                                                  @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;

        return createTickerHelper(type, BlockEntityRegistry.PURPLE_WORM_SPAWNER_BLOCK_ENTITY.get(),
                PurpleWormSpawnerBlockEntity::serverTick);
    }
}
