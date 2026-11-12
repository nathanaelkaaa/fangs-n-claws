package net.raptorzizi.fangs_n_claws.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public final class FncBiomes {

    public static final ResourceKey<Biome> VERMICITE_CAVE =
            ResourceKey.create(Registries.BIOME, FangsClawsMod.id("vermicite_cave"));

    private static volatile boolean buildingVanillaLookup;

    private FncBiomes() { }

    public static void setBuildingVanillaLookup(boolean building) {
        buildingVanillaLookup = building;
    }

    public static boolean isBuildingVanillaLookup() {
        return buildingVanillaLookup;
    }
}
