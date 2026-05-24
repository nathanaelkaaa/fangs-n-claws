package net.raptorzizi.fangs_n_claws.entity.golem;

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
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GolemRenderer extends GeoEntityRenderer<GolemEntity> {

    private static final String LEFT_HAND_BONE = "Left Hand";
    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/golem_eyes.png");

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel());
        this.addRenderLayer(new GolemGrassLayer(this));
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, GolemEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType eyesType = RenderType.eyes(EYES);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    @Override
    protected float getDeathMaxRotation(GolemEntity animatable) {
        return 0f;
    }

    @Override
    public void render(GolemEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        GeoBone leftHand      = null;
        boolean boneWasHidden = false;

        float handScale = entity.getHandScale();
        if (handScale < 1.0f) {
            BakedGeoModel model = this.getGeoModel()
                    .getBakedModel(this.getGeoModel().getModelResource(entity));
            if (model != null) {
                leftHand = model.getBone(LEFT_HAND_BONE).orElse(null);
                if (leftHand != null) {
                    if (handScale <= 0f) {
                        leftHand.setHidden(true);
                        boneWasHidden = true;
                    } else {
                        leftHand.setScaleX(handScale);
                        leftHand.setScaleY(handScale);
                        leftHand.setScaleZ(handScale);
                    }
                }
            }
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if (leftHand != null) {
            if (boneWasHidden) {
                leftHand.setHidden(false);
            } else {
                leftHand.setScaleX(1f);
                leftHand.setScaleY(1f);
                leftHand.setScaleZ(1f);
            }
        }
    }
}
