package net.raptorzizi.fangs_n_claws.entity.cave_ogre;

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

public class CaveOgreRenderer extends GeoEntityRenderer<CaveOgreEntity> {

    private static final float            SCALE        = 1.1F;
    private static final ResourceLocation EYES         =
            FangsClawsMod.id("textures/entity/glowing_eyes/cave_ogre_eyes.png");
    private static final ResourceLocation EYES_SIAMESE =
            FangsClawsMod.id("textures/entity/glowing_eyes/cave_ogre_siamese_eyes.png");

    public CaveOgreRenderer(EntityRendererProvider.Context context) {
        super(context, new CaveOgreModel());
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, CaveOgreEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                ResourceLocation eyesTex = animatable.isSiamese() ? EYES_SIAMESE : EYES;
                RenderType eyesType = RenderType.eyes(eyesTex);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    CaveOgreEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
    }
}
