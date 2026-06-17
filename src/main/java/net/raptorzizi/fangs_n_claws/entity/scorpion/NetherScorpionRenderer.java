package net.raptorzizi.fangs_n_claws.entity.scorpion;

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

public class NetherScorpionRenderer extends GeoEntityRenderer<NetherScorpionEntity> {

    private static final float SCALE = 1.3F;

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/nether_scorpion_eyes.png");

    public NetherScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new NetherScorpionModel());
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, NetherScorpionEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick,
                               int packedLight, int packedOverlay) {
                RenderType eyesType = RenderType.entityTranslucentEmissive(EYES, false);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick,
                        LightTexture.FULL_SKY, packedOverlay, -1);
            }
        });
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    NetherScorpionEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack,
                animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
