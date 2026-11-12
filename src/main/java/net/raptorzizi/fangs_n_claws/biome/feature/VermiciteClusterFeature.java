package net.raptorzizi.fangs_n_claws.biome.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

public class VermiciteClusterFeature extends Feature<DripstoneClusterConfiguration> {

    public VermiciteClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<DripstoneClusterConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        DripstoneClusterConfiguration config = context.config();
        RandomSource random = context.random();

        if (!VermiciteUtils.isEmptyOrWater(level, origin)) return false;

        int height = config.height.sample(random);
        float wetness = config.wetness.sample(random);
        float density = config.density.sample(random);
        int radiusX = config.radius.sample(random);
        int radiusZ = config.radius.sample(random);

        for (int x = -radiusX; x <= radiusX; x++) {
            for (int z = -radiusZ; z <= radiusZ; z++) {
                double chance = this.getChanceOfColumn(radiusX, radiusZ, x, z, config);
                this.placeColumn(level, random, origin.offset(x, 0, z), x, z, wetness, chance, height, density, config);
            }
        }

        return true;
    }

    private void placeColumn(WorldGenLevel level, RandomSource random, BlockPos pos, int x, int z,
                             float wetness, double chance, int height, float density,
                             DripstoneClusterConfiguration config) {
        Optional<Column> scan = Column.scan(level, pos, config.floorToCeilingSearchRange,
                VermiciteUtils::isEmptyOrWater, VermiciteUtils::isNeitherEmptyNorWater);
        if (scan.isEmpty()) return;

        OptionalInt ceiling = scan.get().getCeiling();
        OptionalInt scannedFloor = scan.get().getFloor();
        if (ceiling.isEmpty() && scannedFloor.isEmpty()) return;

        Column column;
        if (random.nextFloat() < wetness && scannedFloor.isPresent()
                && this.canPlacePool(level, pos.atY(scannedFloor.getAsInt()))) {
            int floorY = scannedFloor.getAsInt();
            column = scan.get().withFloor(OptionalInt.of(floorY - 1));
            level.setBlock(pos.atY(floorY), BlocksRegistry.ACID.get().defaultBlockState(), 2);
        } else {
            column = scan.get();
        }

        OptionalInt floor = column.getFloor();

        int stalactite;
        if (ceiling.isPresent() && random.nextDouble() < chance && !this.isLava(level, pos.atY(ceiling.getAsInt()))) {
            int thickness = config.dripstoneBlockLayerThickness.sample(random);
            this.replaceBlocksWithVermicite(level, pos.atY(ceiling.getAsInt()), thickness, Direction.UP);
            int available = floor.isPresent()
                    ? Math.min(height, ceiling.getAsInt() - floor.getAsInt())
                    : height;
            stalactite = this.getColumnHeight(random, x, z, density, available, config);
        } else {
            stalactite = 0;
        }

        int stalagmite;
        if (floor.isPresent() && random.nextDouble() < chance && !this.isLava(level, pos.atY(floor.getAsInt()))) {
            int thickness = config.dripstoneBlockLayerThickness.sample(random);
            this.replaceBlocksWithVermicite(level, pos.atY(floor.getAsInt()), thickness, Direction.DOWN);
            stalagmite = ceiling.isPresent()
                    ? Math.max(0, stalactite + Mth.randomBetweenInclusive(random,
                            -config.maxStalagmiteStalactiteHeightDiff, config.maxStalagmiteStalactiteHeightDiff))
                    : this.getColumnHeight(random, x, z, density, height, config);
        } else {
            stalagmite = 0;
        }

        int finalStalactite;
        int finalStalagmite;
        if (ceiling.isPresent() && floor.isPresent()
                && ceiling.getAsInt() - stalactite <= floor.getAsInt() + stalagmite) {
            int floorY = floor.getAsInt();
            int ceilingY = ceiling.getAsInt();
            int lowest = Math.max(ceilingY - stalactite, floorY + 1);
            int highest = Math.min(floorY + stalagmite, ceilingY - 1);
            int meeting = Mth.randomBetweenInclusive(random, lowest, highest + 1);
            finalStalactite = ceilingY - meeting;
            finalStalagmite = meeting - 1 - floorY;
        } else {
            finalStalactite = stalactite;
            finalStalagmite = stalagmite;
        }

        boolean merge = random.nextBoolean() && finalStalactite > 0 && finalStalagmite > 0
                && column.getHeight().isPresent()
                && finalStalactite + finalStalagmite == column.getHeight().getAsInt();

        if (ceiling.isPresent()) {
            VermiciteUtils.growPointedVermicite(level, pos.atY(ceiling.getAsInt() - 1),
                    Direction.DOWN, finalStalactite, merge);
        }
        if (floor.isPresent()) {
            VermiciteUtils.growPointedVermicite(level, pos.atY(floor.getAsInt() + 1),
                    Direction.UP, finalStalagmite, merge);
        }
    }

    private boolean isLava(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.LAVA);
    }

    private int getColumnHeight(RandomSource random, int x, int z, float chance, int height,
                                DripstoneClusterConfiguration config) {
        if (random.nextFloat() > chance) return 0;

        int distance = Math.abs(x) + Math.abs(z);
        float mean = (float) Mth.clampedMap((double) distance, 0.0,
                (double) config.maxDistanceFromCenterAffectingHeightBias, (double) height / 2.0, 0.0);
        return (int) ClampedNormalFloat.sample(random, mean, (float) config.heightDeviation, 0.0F, (float) height);
    }

    private boolean canPlacePool(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(BlocksRegistry.ACID.get())
                || state.is(BlocksRegistry.VERMICITE.get())
                || state.is(BlocksRegistry.POINTED_VERMICITE.get())) {
            return false;
        }
        if (level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER)) return false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!this.canBeAdjacentToPool(level, pos.relative(direction))) return false;
        }
        return this.canBeAdjacentToPool(level, pos.below());
    }

    private boolean canBeAdjacentToPool(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlocksRegistry.VERMICITE.get())
                || state.is(BlocksRegistry.ACID.get());
    }

    private void replaceBlocksWithVermicite(WorldGenLevel level, BlockPos pos, int thickness, Direction direction) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 0; i < thickness; i++) {
            if (!VermiciteUtils.placeVermiciteBlockIfPossible(level, cursor)) return;
            cursor.move(direction);
        }
    }

    private double getChanceOfColumn(int radiusX, int radiusZ, int x, int z,
                                     DripstoneClusterConfiguration config) {
        int fromEdge = Math.min(radiusX - Math.abs(x), radiusZ - Math.abs(z));
        return Mth.clampedMap((float) fromEdge, 0.0F,
                (float) config.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn,
                config.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
    }
}
