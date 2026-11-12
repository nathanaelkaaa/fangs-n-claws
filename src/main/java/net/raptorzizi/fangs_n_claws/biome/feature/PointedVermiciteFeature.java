package net.raptorzizi.fangs_n_claws.biome.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PointedVermiciteFeature extends Feature<PointedDripstoneConfiguration> {

    private static final int TALL_HEIGHT = 2;
    private static final int SHORT_HEIGHT = 1;

    public PointedVermiciteFeature(Codec<PointedDripstoneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<PointedDripstoneConfiguration> context) {
        LevelAccessor level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        PointedDripstoneConfiguration config = context.config();

        Optional<Direction> tipDirection = getTipDirection(level, origin, random);
        if (tipDirection.isEmpty()) return false;

        Direction direction = tipDirection.get();
        createPatchOfVermiciteBlocks(level, random, origin.relative(direction.getOpposite()), config);

        boolean taller = random.nextFloat() < config.chanceOfTallerDripstone
                && VermiciteUtils.isEmptyOrWater(level.getBlockState(origin.relative(direction)));
        VermiciteUtils.growPointedVermicite(level, origin, direction,
                taller ? TALL_HEIGHT : SHORT_HEIGHT, false);
        return true;
    }

    private static Optional<Direction> getTipDirection(LevelAccessor level, BlockPos pos, RandomSource random) {
        boolean baseAbove = VermiciteUtils.isVermiciteBase(level.getBlockState(pos.above()));
        boolean baseBelow = VermiciteUtils.isVermiciteBase(level.getBlockState(pos.below()));

        if (baseAbove && baseBelow) {
            return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
        }
        if (baseAbove) return Optional.of(Direction.DOWN);
        return baseBelow ? Optional.of(Direction.UP) : Optional.empty();
    }

    private static void createPatchOfVermiciteBlocks(LevelAccessor level, RandomSource random,
                                                     BlockPos pos, PointedDripstoneConfiguration config) {
        VermiciteUtils.placeVermiciteBlockIfPossible(level, pos);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() > config.chanceOfDirectionalSpread) continue;

            BlockPos first = pos.relative(direction);
            VermiciteUtils.placeVermiciteBlockIfPossible(level, first);
            if (random.nextFloat() > config.chanceOfSpreadRadius2) continue;

            BlockPos second = first.relative(Direction.getRandom(random));
            VermiciteUtils.placeVermiciteBlockIfPossible(level, second);
            if (random.nextFloat() > config.chanceOfSpreadRadius3) continue;

            VermiciteUtils.placeVermiciteBlockIfPossible(level, second.relative(Direction.getRandom(random)));
        }
    }
}
