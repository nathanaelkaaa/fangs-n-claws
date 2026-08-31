package net.raptorzizi.fangs_n_claws.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.entity.mimic.MimicEntity;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.GameRuleRegistry;
import net.minecraft.world.entity.MobSpawnType;

public class MimicSpawnerBlock extends Block {

    public static final MapCodec<MimicSpawnerBlock> CODEC = simpleCodec(MimicSpawnerBlock::new);

    public MimicSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public static boolean mimicsAllowed(ServerLevelAccessor level) {
        return CommonConfigs.ALLOW_SPAWN_MIMIC.get()
                && level.getLevel().getGameRules().getBoolean(GameRuleRegistry.ALLOW_SPAWN_MIMIC);
    }

    public static void scheduleAbove(ServerLevelAccessor level, BlockPos chestPos) {
        if (!mimicsAllowed(level)) return;
        BlockPos above = chestPos.above();
        BlockState there = level.getBlockState(above);
        if (there.isAir() || there.is(Blocks.CHEST)) {
            Block marker = BlocksRegistry.MIMIC_SPAWNER.get();
            level.setBlock(above, marker.defaultBlockState(), 3);
            level.scheduleTick(above, marker, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos chestPos = pos.below();
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        if (!mimicsAllowed(level)) return;

        if (!level.getBlockState(chestPos).is(Blocks.CHEST)) return;

        int chance = CommonConfigs.MIMIC_SPAWN_CHANCE.get();
        if (chance < 1 || random.nextInt(chance) != 0) return;

        MimicEntity mimic = EntityRegistry.MIMIC.get().create(level);
        if (mimic == null) return;
        mimic.setPos(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5);
        mimic.finalizeSpawn(level, level.getCurrentDifficultyAt(chestPos),
                MobSpawnType.STRUCTURE, null);
        level.addFreshEntity(mimic);
        mimic.consumeChest(chestPos);
    }
}
