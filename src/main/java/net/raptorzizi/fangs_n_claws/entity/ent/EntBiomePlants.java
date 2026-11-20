package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class EntBiomePlants {

    // Sampling
    private static final long SEED = 0x5E4D1E57L;
    private static final int SAMPLES = 32;
    private static final int SAMPLE_SPACING = 97;

    private static final Map<String, String> LEAVES_TO_SAPLING = Map.of(
            "mangrove_leaves", "mangrove_propagule",
            "azalea_leaves", "azalea",
            "flowering_azalea_leaves", "flowering_azalea");

    private static final Map<Biome, Plants> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public record Plants(List<Block> saplings, List<Block> flowers, List<Block> mushrooms) {

        public Block pick(RandomSource random) {
            List<List<Block>> families = new ArrayList<>(3);
            if (!this.saplings.isEmpty()) families.add(this.saplings);
            if (!this.flowers.isEmpty()) families.add(this.flowers);
            if (!this.mushrooms.isEmpty()) families.add(this.mushrooms);
            if (families.isEmpty()) return null;
            List<Block> family = families.get(random.nextInt(families.size()));
            return family.get(random.nextInt(family.size()));
        }
    }

    private EntBiomePlants() { }

    public static Plants of(Holder<Biome> biome) {
        return CACHE.computeIfAbsent(biome.value(), EntBiomePlants::scan);
    }

    private static Plants scan(Biome biome) {
        Set<Block> saplings = new LinkedHashSet<>();
        Set<Block> flowers = new LinkedHashSet<>();
        Set<Block> mushrooms = new LinkedHashSet<>();
        RandomSource random = RandomSource.create(SEED);

        for (HolderSet<PlacedFeature> step : biome.getGenerationSettings().features()) {
            for (Holder<PlacedFeature> placed : step) {
                placed.value().getFeatures().forEach(feature ->
                        collect(feature, random, saplings, flowers, mushrooms));
            }
        }
        return new Plants(List.copyOf(saplings), List.copyOf(flowers), List.copyOf(mushrooms));
    }

    private static void collect(ConfiguredFeature<?, ?> feature, RandomSource random,
                                Set<Block> saplings, Set<Block> flowers, Set<Block> mushrooms) {
        FeatureConfiguration config = feature.config();
        if (config instanceof TreeConfiguration tree) {
            for (BlockState leaves : sample(tree.foliageProvider, random)) {
                Block sapling = saplingFor(leaves);
                if (sapling != null) saplings.add(sapling);
            }
        } else if (config instanceof SimpleBlockConfiguration simple) {
            for (BlockState state : sample(simple.toPlace(), random)) {
                if (isMushroom(state.getBlock())) {
                    mushrooms.add(state.getBlock());
                } else if ((state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.TALL_FLOWERS))
                        && !state.is(Blocks.WITHER_ROSE)) {
                    flowers.add(state.getBlock());
                } else if (state.is(BlockTags.SAPLINGS)) {
                    saplings.add(state.getBlock());
                }
            }
        } else if (config instanceof HugeMushroomFeatureConfiguration huge) {
            for (BlockState cap : sample(huge.capProvider, random)) {
                if (cap.is(Blocks.RED_MUSHROOM_BLOCK)) mushrooms.add(Blocks.RED_MUSHROOM);
                if (cap.is(Blocks.BROWN_MUSHROOM_BLOCK)) mushrooms.add(Blocks.BROWN_MUSHROOM);
            }
        }
    }

    private static Set<BlockState> sample(BlockStateProvider provider, RandomSource random) {
        Set<BlockState> states = new LinkedHashSet<>();
        for (int i = 0; i < SAMPLES; i++) {
            states.add(provider.getState(random, new BlockPos(i * SAMPLE_SPACING, 0, i * SAMPLE_SPACING * 3)));
        }
        return states;
    }

    public static boolean isMushroom(Block block) {
        return block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM;
    }

    private static Block saplingFor(BlockState leaves) {
        if (!leaves.is(BlockTags.LEAVES)) return null;
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(leaves.getBlock());
        String path = LEAVES_TO_SAPLING.getOrDefault(key.getPath(), key.getPath().replace("_leaves", "_sapling"));
        Block sapling = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), path));
        return sapling.defaultBlockState().is(BlockTags.SAPLINGS) ? sapling : null;
    }
}
