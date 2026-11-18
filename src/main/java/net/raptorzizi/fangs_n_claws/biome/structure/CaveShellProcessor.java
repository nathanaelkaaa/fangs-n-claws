package net.raptorzizi.fangs_n_claws.biome.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.raptorzizi.fangs_n_claws.registries.StructureProcessorRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CaveShellProcessor extends StructureProcessor {

    public static final MapCodec<CaveShellProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter(processor -> processor.shell)
    ).apply(instance, CaveShellProcessor::new));

    private final List<Block> shell;
    private final Set<Block> shellSet;

    public CaveShellProcessor(List<Block> shell) {
        this.shell = List.copyOf(shell);
        this.shellSet = Set.copyOf(shell);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
            ServerLevelAccessor level, BlockPos offset, BlockPos pos,
            List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
            List<StructureTemplate.StructureBlockInfo> processedBlockInfos,
            StructurePlaceSettings settings) {

        Map<BlockPos, BlockState> template = new HashMap<>(processedBlockInfos.size());
        for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
            template.put(info.pos(), info.state());
        }

        List<StructureTemplate.StructureBlockInfo> kept = new ArrayList<>(processedBlockInfos.size());
        for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
            boolean opened = this.isShell(info.state())
                    && level.getBlockState(info.pos()).is(BlockTags.AIR)
                    && !this.holdsDecoration(template, info.pos());
            if (!opened) kept.add(info);
        }
        return kept;
    }

    private boolean holdsDecoration(Map<BlockPos, BlockState> template, BlockPos at) {
        for (Direction direction : Direction.values()) {
            BlockState neighbour = template.get(at.relative(direction));
            if (neighbour != null && !neighbour.isAir() && !this.isShell(neighbour)) return true;
        }
        return false;
    }

    private boolean isShell(BlockState state) {
        return this.shellSet.contains(state.getBlock());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorRegistry.CAVE_SHELL.get();
    }
}
