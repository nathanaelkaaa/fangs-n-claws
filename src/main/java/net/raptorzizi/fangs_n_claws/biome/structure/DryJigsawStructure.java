package net.raptorzizi.fangs_n_claws.biome.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.raptorzizi.fangs_n_claws.registries.StructureTypeRegistry;

import java.util.Optional;

public class DryJigsawStructure extends Structure {

    public static final MapCodec<DryJigsawStructure> CODEC =
            JigsawStructure.CODEC.xmap(DryJigsawStructure::new, structure -> structure.jigsaw);

    // Water probe
    private static final int PROBE_REACH = 11;
    private static final int[][] PROBES = {
            { 0, 0 },
            { -PROBE_REACH, -PROBE_REACH }, { PROBE_REACH, -PROBE_REACH },
            { -PROBE_REACH, PROBE_REACH }, { PROBE_REACH, PROBE_REACH },
            { 0, -PROBE_REACH }, { 0, PROBE_REACH }, { -PROBE_REACH, 0 }, { PROBE_REACH, 0 } };

    private final JigsawStructure jigsaw;

    public DryJigsawStructure(JigsawStructure jigsaw) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(), jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return this.jigsaw.findValidGenerationPoint(context).filter(stub -> isDry(context, stub.position()));
    }

    private static boolean isDry(GenerationContext context, BlockPos center) {
        ChunkGenerator generator = context.chunkGenerator();
        for (int[] probe : PROBES) {
            int x = center.getX() + probe[0];
            int z = center.getZ() + probe[1];
            int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            if (surface > floor) return false;
        }
        return true;
    }

    @Override
    public StructureType<?> type() {
        return StructureTypeRegistry.DRY_JIGSAW.get();
    }
}
