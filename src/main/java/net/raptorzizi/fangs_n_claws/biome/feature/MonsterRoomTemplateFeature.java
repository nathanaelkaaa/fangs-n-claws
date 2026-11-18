package net.raptorzizi.fangs_n_claws.biome.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class MonsterRoomTemplateFeature extends Feature<MonsterRoomTemplateFeature.Config> {

    // Vanilla rule
    private static final int MIN_OPENINGS = 1;
    private static final int MAX_OPENINGS = 5;

    public record Config(ResourceLocation template) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("template").forGetter(Config::template)
        ).apply(instance, Config::new));
    }

    public MonsterRoomTemplateFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();

        Optional<StructureTemplate> found = TemplateRoomPlacer.template(level, context.config().template());
        if (found.isEmpty()) return false;
        StructureTemplate template = found.get();

        BlockPos pivot = TemplateRoomPlacer.pivot(template);
        BlockPos corner = context.origin().offset(-pivot.getX(), -1, -pivot.getZ());
        StructurePlaceSettings settings = TemplateRoomPlacer.settings(template, context.random());
        BoundingBox box = template.getBoundingBox(settings, corner);

        if (!fitsLikeVanilla(level, box)) return false;
        if (TemplateRoomPlacer.nearWater(level, box)) return false;

        TemplateRoomPlacer.place(context, template, settings, corner, box);
        return true;
    }

    private static boolean fitsLikeVanilla(WorldGenLevel level, BoundingBox box) {
        int floorY = box.minY();
        int ceilingY = box.maxY();
        int openings = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (!level.getBlockState(cursor.set(x, floorY, z)).isSolid()) return false;
                if (!level.getBlockState(cursor.set(x, ceilingY, z)).isSolid()) return false;

                boolean rim = x == box.minX() || x == box.maxX() || z == box.minZ() || z == box.maxZ();
                if (rim
                        && level.isEmptyBlock(cursor.set(x, floorY + 1, z))
                        && level.isEmptyBlock(cursor.set(x, floorY + 2, z))) {
                    openings++;
                }
            }
        }

        return openings >= MIN_OPENINGS && openings <= MAX_OPENINGS;
    }
}
