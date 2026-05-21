package net.raptorzizi.fangs_n_claws.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;

public final class BiomeUtils {

    private BiomeUtils() {}

    public static String resolveBiomeFolder(ServerLevelAccessor level, BlockPos pos) {
        ResourceLocation biome = level.getBiome(pos).unwrapKey()
                .map(k -> k.location())
                .orElse(null);

        if (biome == null) return "plains";

        String path = biome.getPath();

        if (path.contains("taiga"))                             return "taiga";
        if (path.contains("desert"))                            return "desert";
        if (path.contains("jungle"))                            return "jungle";
        if (path.contains("savanna"))                           return "savanna";
        if (path.contains("snowy") || path.contains("frozen")) return "snowy";
        if (path.contains("swamp"))                             return "swamp";
        if (path.contains("badlands"))                          return "desert";

        return "plains";
    }
}
