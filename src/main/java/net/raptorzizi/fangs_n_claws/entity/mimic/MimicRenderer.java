package net.raptorzizi.fangs_n_claws.entity.mimic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MimicRenderer extends GeoEntityRenderer<MimicEntity> {

    public MimicRenderer(EntityRendererProvider.Context context) {
        super(context, new MimicModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new MimicOverlayLayer(this));
    }

    @Override
    protected void applyRotations(MimicEntity animatable, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
    }

    private static class MimicOverlayLayer extends GeoRenderLayer<MimicEntity> {

        private static final ResourceLocation MIMIC_TEXTURE =
                FangsClawsMod.id("textures/entity/mimic.png");

        MimicOverlayLayer(GeoRenderer<MimicEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MimicEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource,
                           VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderType     overlayType   = RenderType.entityCutoutNoCull(MIMIC_TEXTURE);
            VertexConsumer overlayBuffer = bufferSource.getBuffer(overlayType);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                    overlayType, overlayBuffer, partialTick, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
