package net.raptorzizi.fangs_n_claws.biome.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.raptorzizi.fangs_n_claws.registries.StructurePoolElementRegistry;

import java.util.Optional;

public class GroundedSinglePoolElement extends SinglePoolElement {

    public static final MapCodec<GroundedSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            templateCodec(),
            processorsCodec(),
            projectionCodec(),
            overrideLiquidSettingsCodec(),
            Codec.intRange(0, 64).fieldOf("ground_y").forGetter(element -> element.groundY)
    ).apply(instance, GroundedSinglePoolElement::new));

    private final int groundY;

    protected GroundedSinglePoolElement(Either<ResourceLocation, StructureTemplate> template,
                                        Holder<StructureProcessorList> processors,
                                        StructureTemplatePool.Projection projection,
                                        Optional<LiquidSettings> overrideLiquidSettings,
                                        int groundY) {
        super(template, processors, projection, overrideLiquidSettings);
        this.groundY = groundY;
    }

    @Override
    public int getGroundLevelDelta() {
        return this.groundY + 1;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementRegistry.GROUNDED_SINGLE.get();
    }

    @Override
    public String toString() {
        return "GroundedSingle[" + this.template + ", ground_y=" + this.groundY + "]";
    }
}
