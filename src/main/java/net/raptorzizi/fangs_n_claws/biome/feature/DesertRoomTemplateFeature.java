package net.raptorzizi.fangs_n_claws.biome.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class DesertRoomTemplateFeature extends Feature<DesertRoomTemplateFeature.Config> {

    // Vanilla rule
    private static final int MIN_HEIGHT_ABOVE_BOTTOM = 2;

    public record Config(ResourceLocation template, int sink) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("template").forGetter(Config::template),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("sink").forGetter(Config::sink)
        ).apply(instance, Config::new));
    }

    public DesertRoomTemplateFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();

        Optional<StructureTemplate> found = TemplateRoomPlacer.template(level, context.config().template());
        if (found.isEmpty()) return false;
        StructureTemplate template = found.get();

        BlockPos ground = context.origin().above();
        while (level.isEmptyBlock(ground) && ground.getY() > level.getMinBuildHeight() + MIN_HEIGHT_ABOVE_BOTTOM) {
            ground = ground.below();
        }
        if (!level.getBlockState(ground).is(Blocks.SAND)) return false;

        BlockPos pivot = TemplateRoomPlacer.pivot(template);
        BlockPos corner = ground.offset(-pivot.getX(), -context.config().sink(), -pivot.getZ());
        StructurePlaceSettings settings = TemplateRoomPlacer.settings(template, context.random());
        BoundingBox box = template.getBoundingBox(settings, corner);

        if (!supported(level, box, ground.getY())) return false;
        if (TemplateRoomPlacer.nearWater(level, box)) return false;

        TemplateRoomPlacer.place(context, template, settings, corner, box);
        return true;
    }

    private static boolean supported(WorldGenLevel level, BoundingBox box, int groundY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (level.isEmptyBlock(cursor.set(x, groundY - 1, z))
                        && level.isEmptyBlock(cursor.set(x, groundY - 2, z))) {
                    return false;
                }
            }
        }

        return true;
    }
}
