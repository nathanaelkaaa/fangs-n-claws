package net.raptorzizi.fangs_n_claws.biome;

import net.minecraft.world.level.biome.Climate;

public final class VermiciteCavePlacement {

    // Mangrove swamp climate window
    private static final long TEMPERATURE_MIN = Climate.quantizeCoord(0.2F);
    private static final long TEMPERATURE_MAX = Climate.quantizeCoord(1.0F);
    private static final long CONTINENTALNESS_MIN = Climate.quantizeCoord(-0.11F);
    private static final long CONTINENTALNESS_MAX = Climate.quantizeCoord(1.0F);
    private static final long EROSION_MIN = Climate.quantizeCoord(0.55F);
    private static final long EROSION_MAX = Climate.quantizeCoord(1.0F);

    // Weirdness slices that declare the mangrove swamp
    private static final long WEIRDNESS_INNER = Climate.quantizeCoord(0.4F);
    private static final long WEIRDNESS_OUTER = Climate.quantizeCoord(0.93333334F);

    // Depth
    private static final long DEPTH_MIN = Climate.quantizeCoord(0.8F);

    public static final int MAX_BLOCK_Y = 48;

    private VermiciteCavePlacement() { }

    public static boolean matches(Climate.TargetPoint point) {
        return point.depth() >= DEPTH_MIN
                && within(point.temperature(), TEMPERATURE_MIN, TEMPERATURE_MAX)
                && within(point.continentalness(), CONTINENTALNESS_MIN, CONTINENTALNESS_MAX)
                && within(point.erosion(), EROSION_MIN, EROSION_MAX)
                && mangroveSlice(point.weirdness());
    }

    private static boolean mangroveSlice(long weirdness) {
        long magnitude = Math.abs(weirdness);
        return magnitude <= WEIRDNESS_INNER || magnitude >= WEIRDNESS_OUTER;
    }

    private static boolean within(long value, long min, long max) {
        return value >= min && value <= max;
    }
}
