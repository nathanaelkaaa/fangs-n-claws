package net.raptorzizi.fangs_n_claws.biome.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.raptorzizi.fangs_n_claws.block.PointedVermiciteBlock;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;

import java.util.function.Consumer;

public final class VermiciteUtils {

    private static final double HEIGHT_CONSTANT = 0.384;
    private static final float CIRCLE_SAMPLES = 6.0F;

    private VermiciteUtils() { }

    static double getVermiciteHeight(double radius, double maxRadius, double scale, double minRadius) {
        if (radius < minRadius) {
            radius = minRadius;
        }

        double normalized = radius / maxRadius * HEIGHT_CONSTANT;
        double a = 0.75 * Math.pow(normalized, 1.3333333333333333);
        double b = Math.pow(normalized, 0.6666666666666666);
        double c = 0.3333333333333333 * Math.log(normalized);
        double height = Math.max(scale * (a - b - c), 0.0);
        return height / HEIGHT_CONSTANT * maxRadius;
    }

    static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel level, BlockPos pos, int radius) {
        if (isEmptyOrWaterOrLava(level, pos)) {
            return false;
        }

        float step = CIRCLE_SAMPLES / (float) radius;
        for (float angle = 0.0F; angle < (float) (Math.PI * 2); angle += step) {
            int dx = (int) (Mth.cos(angle) * (float) radius);
            int dz = (int) (Mth.sin(angle) * (float) radius);
            if (isEmptyOrWaterOrLava(level, pos.offset(dx, 0, dz))) {
                return false;
            }
        }

        return true;
    }

    static boolean isEmptyOrWater(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, VermiciteUtils::isEmptyOrWater);
    }

    static boolean isEmptyOrWaterOrLava(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, VermiciteUtils::isEmptyOrWaterOrLava);
    }

    static void buildBaseToTipColumn(Direction direction, int height, boolean mergeTip,
                                     Consumer<BlockState> blockSetter) {
        if (height >= 3) {
            blockSetter.accept(createPointedVermicite(direction, DripstoneThickness.BASE));
            for (int i = 0; i < height - 3; i++) {
                blockSetter.accept(createPointedVermicite(direction, DripstoneThickness.MIDDLE));
            }
        }

        if (height >= 2) {
            blockSetter.accept(createPointedVermicite(direction, DripstoneThickness.FRUSTUM));
        }

        if (height >= 1) {
            blockSetter.accept(createPointedVermicite(direction,
                    mergeTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }
    }

    static void growPointedVermicite(LevelAccessor level, BlockPos pos, Direction direction,
                                     int height, boolean mergeTip) {
        if (!isVermiciteBase(level.getBlockState(pos.relative(direction.getOpposite())))) return;

        BlockPos.MutableBlockPos cursor = pos.mutable();
        buildBaseToTipColumn(direction, height, mergeTip, state -> {
            if (state.is(BlocksRegistry.POINTED_VERMICITE.get())) {
                state = state.setValue(PointedVermiciteBlock.WATERLOGGED, level.isWaterAt(cursor));
            }
            level.setBlock(cursor, state, 2);
            cursor.move(direction);
        });
    }

    static boolean placeVermiciteBlockIfPossible(LevelAccessor level, BlockPos pos) {
        if (level.getBlockState(pos).is(BlockTags.DRIPSTONE_REPLACEABLE)) {
            level.setBlock(pos, BlocksRegistry.VERMICITE.get().defaultBlockState(), 2);
            return true;
        }
        return false;
    }

    private static BlockState createPointedVermicite(Direction direction, DripstoneThickness thickness) {
        return BlocksRegistry.POINTED_VERMICITE.get()
                .defaultBlockState()
                .setValue(PointedVermiciteBlock.TIP_DIRECTION, direction)
                .setValue(PointedVermiciteBlock.THICKNESS, thickness);
    }

    public static boolean isVermiciteBaseOrLava(BlockState state) {
        return isVermiciteBase(state) || state.is(Blocks.LAVA);
    }

    public static boolean isVermiciteBase(BlockState state) {
        return state.is(BlocksRegistry.VERMICITE.get()) || state.is(BlockTags.DRIPSTONE_REPLACEABLE);
    }

    public static boolean isEmptyOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }

    public static boolean isNeitherEmptyNorWater(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER);
    }

    public static boolean isEmptyOrWaterOrLava(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}
