package net.raptorzizi.fangs_n_claws.biome.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LargeVermiciteFeature extends Feature<LargeDripstoneConfiguration> {

    private static final int MIN_CAVE_HEIGHT = 4;
    private static final int MAX_EMBED_SEARCH = 10;
    private static final double SHORTEN_CHANCE = 0.2;

    public LargeVermiciteFeature(Codec<LargeDripstoneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<LargeDripstoneConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        LargeDripstoneConfiguration config = context.config();
        RandomSource random = context.random();

        if (!VermiciteUtils.isEmptyOrWater(level, origin)) return false;

        Optional<Column> scan = Column.scan(level, origin, config.floorToCeilingSearchRange,
                VermiciteUtils::isEmptyOrWater, VermiciteUtils::isVermiciteBaseOrLava);
        if (scan.isEmpty() || !(scan.get() instanceof Column.Range range)) return false;
        if (range.height() < MIN_CAVE_HEIGHT) return false;

        int ratioRadius = (int) ((float) range.height() * config.maxColumnRadiusToCaveHeightRatio);
        int maxRadius = Mth.clamp(ratioRadius,
                config.columnRadius.getMinValue(), config.columnRadius.getMaxValue());
        int radius = Mth.randomBetweenInclusive(random, config.columnRadius.getMinValue(), maxRadius);

        Spike stalactite = makeSpike(origin.atY(range.ceiling() - 1), false, random, radius,
                config.stalactiteBluntness, config.heightScale);
        Spike stalagmite = makeSpike(origin.atY(range.floor() + 1), true, random, radius,
                config.stalagmiteBluntness, config.heightScale);

        WindOffsetter wind = stalactite.isSuitableForWind(config) && stalagmite.isSuitableForWind(config)
                ? new WindOffsetter(origin.getY(), random, config.windSpeed)
                : WindOffsetter.noWind();

        boolean stalactiteAnchored = stalactite.moveBackUntilBaseIsInsideStone(level, wind);
        boolean stalagmiteAnchored = stalagmite.moveBackUntilBaseIsInsideStone(level, wind);

        if (stalactiteAnchored) stalactite.placeBlocks(level, random, wind);
        if (stalagmiteAnchored) stalagmite.placeBlocks(level, random, wind);
        return true;
    }

    private static Spike makeSpike(BlockPos root, boolean pointingUp, RandomSource random, int radius,
                                   FloatProvider bluntnessBase, FloatProvider scaleBase) {
        return new Spike(root, pointingUp, radius, bluntnessBase.sample(random), scaleBase.sample(random));
    }

    private static final class Spike {

        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        private Spike(BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        private int getHeightAtRadius(float radius) {
            return (int) VermiciteUtils.getVermiciteHeight(radius, this.radius, this.scale, this.bluntness);
        }

        private boolean moveBackUntilBaseIsInsideStone(WorldGenLevel level, WindOffsetter wind) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos cursor = this.root.mutable();
                int steps = Math.min(MAX_EMBED_SEARCH, this.getHeight());

                for (int i = 0; i < steps; i++) {
                    if (level.getBlockState(cursor).is(Blocks.LAVA)) return false;

                    if (VermiciteUtils.isCircleMostlyEmbeddedInStone(level, wind.offset(cursor), this.radius)) {
                        this.root = cursor;
                        return true;
                    }

                    cursor.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }

                this.radius /= 2;
            }

            return false;
        }

        private void placeBlocks(WorldGenLevel level, RandomSource random, WindOffsetter wind) {
            for (int x = -this.radius; x <= this.radius; x++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    float distance = Mth.sqrt((float) (x * x + z * z));
                    if (distance > (float) this.radius) continue;

                    int height = this.getHeightAtRadius(distance);
                    if (height <= 0) continue;

                    if ((double) random.nextFloat() < SHORTEN_CHANCE) {
                        height = (int) ((float) height * Mth.randomBetween(random, 0.8F, 1.0F));
                    }

                    BlockPos.MutableBlockPos cursor = this.root.offset(x, 0, z).mutable();
                    boolean started = false;
                    int ceilingLimit = this.pointingUp
                            ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cursor.getX(), cursor.getZ())
                            : Integer.MAX_VALUE;

                    for (int i = 0; i < height && cursor.getY() < ceilingLimit; i++) {
                        BlockPos pos = wind.offset(cursor);
                        if (VermiciteUtils.isEmptyOrWaterOrLava(level, pos)) {
                            started = true;
                            level.setBlock(pos, BlocksRegistry.VERMICITE.get().defaultBlockState(), 2);
                        } else if (started && level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                            break;
                        }

                        cursor.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                    }
                }
            }
        }

        private boolean isSuitableForWind(LargeDripstoneConfiguration config) {
            return this.radius >= config.minRadiusForWind
                    && this.bluntness >= (double) config.minBluntnessForWind;
        }
    }

    private static final class WindOffsetter {

        private final int originY;
        @Nullable
        private final Vec3 windSpeed;

        private WindOffsetter(int originY, RandomSource random, FloatProvider magnitude) {
            this.originY = originY;
            float speed = magnitude.sample(random);
            float angle = Mth.randomBetween(random, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3(Mth.cos(angle) * speed, 0.0, Mth.sin(angle) * speed);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        private static WindOffsetter noWind() {
            return new WindOffsetter();
        }

        private BlockPos offset(BlockPos pos) {
            if (this.windSpeed == null) return pos;

            Vec3 drift = this.windSpeed.scale(this.originY - pos.getY());
            return pos.offset(Mth.floor(drift.x), 0, Mth.floor(drift.z));
        }
    }
}
