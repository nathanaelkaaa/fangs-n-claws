package net.raptorzizi.fangs_n_claws.biome.feature;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import net.raptorzizi.fangs_n_claws.registries.FluidRegistry;

public class JadeOreFeature extends Feature<NoneFeatureConfiguration> {

    // Probing
    private static final int PROBES = 64;
    private static final int RADIUS = 10;

    // Yield
    private static final float DRY_CHANCE = 0.35F;
    private static final int DRY_MAX_ORES = 1;
    private static final float ACID_CHANCE = 0.9F;
    private static final int ACID_MAX_ORES = 2;

    public JadeOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        List<BlockPos> vermicite = new ArrayList<>();
        boolean acidNearby = false;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int i = 0; i < PROBES; i++) {
            probe.set(origin.getX() + offset(random),
                    origin.getY() + offset(random),
                    origin.getZ() + offset(random));

            if (level.getFluidState(probe).getFluidType() == FluidRegistry.ACID_TYPE.get()) {
                acidNearby = true;
            } else if (level.getBlockState(probe).is(BlocksRegistry.VERMICITE.get())) {
                vermicite.add(probe.immutable());
            }
        }

        if (vermicite.isEmpty()) return false;

        float chance = acidNearby ? ACID_CHANCE : DRY_CHANCE;
        int budget = acidNearby ? ACID_MAX_ORES : DRY_MAX_ORES;
        boolean placed = false;

        for (BlockPos pos : vermicite) {
            if (budget <= 0) break;
            if (random.nextFloat() >= chance) continue;

            this.setBlock(level, pos, BlocksRegistry.JADE_ORE.get().defaultBlockState());
            budget--;
            placed = true;
        }

        return placed;
    }

    private static int offset(RandomSource random) {
        return random.nextInt(RADIUS * 2 + 1) - RADIUS;
    }
}
