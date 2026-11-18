package net.raptorzizi.fangs_n_claws.biome.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.raptorzizi.fangs_n_claws.biome.structure.FluidDrainProcessor;

import java.util.List;
import java.util.Optional;

public final class TemplateRoomPlacer {

    // Placement
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;
    private static final String EMPTY_POOL = "minecraft:empty";

    // Water
    private static final int WATER_MARGIN = 8;

    private TemplateRoomPlacer() { }

    public static StructureTemplateManager manager(WorldGenLevel level) {
        return level.getLevel().getServer().getStructureManager();
    }

    public static Optional<StructureTemplate> template(WorldGenLevel level, ResourceLocation id) {
        return manager(level).get(id);
    }

    public static StructurePlaceSettings settings(StructureTemplate template, RandomSource random) {
        return new StructurePlaceSettings()
                .setRotation(Rotation.getRandom(random))
                .setRotationPivot(pivot(template))
                .setRandom(random);
    }

    public static BlockPos pivot(StructureTemplate template) {
        Vec3i size = template.getSize();
        return new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
    }

    public static boolean nearWater(WorldGenLevel level, BoundingBox box) {
        int minY = Math.max(box.minY() - 1, level.getMinBuildHeight());
        int maxY = Math.min(box.maxY() + WATER_MARGIN, level.getMaxBuildHeight() - 1);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            for (int z = box.minZ() - WATER_MARGIN; z <= box.maxZ() + WATER_MARGIN; z++) {
                for (int x = box.minX() - WATER_MARGIN; x <= box.maxX() + WATER_MARGIN; x++) {
                    if (level.getFluidState(cursor.set(x, y, z)).is(FluidTags.WATER)) return true;
                }
            }
        }

        return false;
    }

    public static void place(FeaturePlaceContext<?> context, StructureTemplate template,
                             StructurePlaceSettings settings, BlockPos corner, BoundingBox box) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        settings.setBoundingBox(box)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
                .addProcessor(new FluidDrainProcessor())
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(JigsawReplacementProcessor.INSTANCE)
                .addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE));

        List<StructureTemplate.StructureBlockInfo> jigsaws = template.filterBlocks(corner, settings, Blocks.JIGSAW);
        template.placeInWorld(level, corner, corner, settings, random, PLACE_FLAGS);

        for (StructureTemplate.StructureBlockInfo jigsaw : jigsaws) {
            resolveJigsaw(context, jigsaw, box);
        }
    }

    private static void resolveJigsaw(FeaturePlaceContext<?> context, StructureTemplate.StructureBlockInfo jigsaw,
                                      BoundingBox box) {
        if (jigsaw.nbt() == null) return;

        String poolId = jigsaw.nbt().getString("pool");
        if (poolId.isEmpty() || poolId.equals(EMPTY_POOL)) return;

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        StructureTemplatePool pool = level.registryAccess()
                .registryOrThrow(Registries.TEMPLATE_POOL)
                .get(ResourceLocation.parse(poolId));
        if (pool == null) return;

        StructurePoolElement element = pool.getRandomTemplate(random);
        if (element == EmptyPoolElement.INSTANCE) return;

        StructureTemplateManager manager = manager(level);
        BlockPos attach = jigsaw.pos().relative(JigsawBlock.getFrontFacing(jigsaw.state()));

        for (Rotation rotation : Rotation.getShuffled(random)) {
            for (StructureTemplate.StructureBlockInfo child
                    : element.getShuffledJigsawBlocks(manager, BlockPos.ZERO, rotation, random)) {
                if (!JigsawBlock.canAttach(jigsaw, child)) continue;

                BlockPos offset = attach.subtract(child.pos());
                element.place(manager, level, level.getLevel().structureManager(), context.chunkGenerator(),
                        offset, offset, rotation, box, random, LiquidSettings.IGNORE_WATERLOGGING, false);
                return;
            }
        }
    }
}
