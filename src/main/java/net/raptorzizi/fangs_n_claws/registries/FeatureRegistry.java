package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.biome.feature.LargeVermiciteFeature;
import net.raptorzizi.fangs_n_claws.biome.feature.PointedVermiciteFeature;
import net.raptorzizi.fangs_n_claws.biome.feature.VermiciteClusterFeature;

public class FeatureRegistry {

    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, FangsClawsMod.MOD_ID);

    // Vermicite
    public static final DeferredHolder<Feature<?>, VermiciteClusterFeature> VERMICITE_CLUSTER =
            FEATURES.register("vermicite_cluster",
                    () -> new VermiciteClusterFeature(DripstoneClusterConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, LargeVermiciteFeature> LARGE_VERMICITE =
            FEATURES.register("large_vermicite",
                    () -> new LargeVermiciteFeature(LargeDripstoneConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, PointedVermiciteFeature> POINTED_VERMICITE =
            FEATURES.register("pointed_vermicite",
                    () -> new PointedVermiciteFeature(PointedDripstoneConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
