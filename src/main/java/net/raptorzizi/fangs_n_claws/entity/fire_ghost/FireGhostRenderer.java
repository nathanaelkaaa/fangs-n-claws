package net.raptorzizi.fangs_n_claws.entity.fire_ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class FireGhostRenderer extends GeoEntityRenderer<FireGhostEntity> {

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/angry_fire_ghost_eyes.png");

    public FireGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new FireGhostModel());
        this.shadowRadius = 0.6f;
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, FireGhostEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (!animatable.isAngry()) return;
                RenderType eyesType = RenderType.eyes(EYES);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay, -1);
            }
        });
    }

    @Override
    public void render(FireGhostEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.5f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    protected float getDeathMaxRotation(FireGhostEntity animatable) {
        return 0f;
    }
}
