package net.raptorzizi.fangs_n_claws.entity.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HorseArmorLayer<T extends HorseMob> extends GeoRenderLayer<T> {

    private static final ResourceLocation BLANKET = FangsClawsMod.id("textures/entity/horse_blanket.png");

    public HorseArmorLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
                       @Nullable RenderType renderType, MultiBufferSource bufferSource,
                       @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!animatable.isWearingArmor()) return;

        RenderType armorType = RenderType.entityCutoutNoCull(BLANKET);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, armorType,
                bufferSource.getBuffer(armorType), partialTick, packedLight, packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}
