package net.raptorzizi.fangs_n_claws.biome.structure;

import java.util.List;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.raptorzizi.fangs_n_claws.registries.StructureProcessorRegistry;

public class FluidDrainProcessor extends StructureProcessor {

    public static final MapCodec<FluidDrainProcessor> CODEC =
            MapCodec.unit(FluidDrainProcessor::new);

    private static final int SET_BLOCK_FLAGS = 2;

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
            ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos,
            List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
            List<StructureTemplate.StructureBlockInfo> processedBlockInfos,
            StructurePlaceSettings settings) {

        drain(serverLevel, processedBlockInfos, settings);

        return processedBlockInfos;
    }

    private static void drain(ServerLevelAccessor level,
                              List<StructureTemplate.StructureBlockInfo> infos,
                              StructurePlaceSettings settings) {
        if (infos.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (StructureTemplate.StructureBlockInfo info : infos) {
            BlockPos at = info.pos();
            minX = Math.min(minX, at.getX());
            minY = Math.min(minY, at.getY());
            minZ = Math.min(minZ, at.getZ());
            maxX = Math.max(maxX, at.getX());
            maxY = Math.max(maxY, at.getY());
            maxZ = Math.max(maxZ, at.getZ());
        }

        BoundingBox writable = settings.getBoundingBox();
        if (writable != null) {
            minX = Math.max(minX, writable.minX());
            minY = Math.max(minY, writable.minY());
            minZ = Math.max(minZ, writable.minZ());
            maxX = Math.min(maxX, writable.maxX());
            maxY = Math.min(maxY, writable.maxY());
            maxZ = Math.min(maxZ, writable.maxZ());
        }

        if (minX > maxX || minY > maxY || minZ > maxZ) return;

        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    at.set(x, y, z);
                    if (level.getFluidState(at).isEmpty()) continue;

                    level.setBlock(at, Blocks.AIR.defaultBlockState(), SET_BLOCK_FLAGS);
                }
            }
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorRegistry.FLUID_DRAIN.get();
    }
}
