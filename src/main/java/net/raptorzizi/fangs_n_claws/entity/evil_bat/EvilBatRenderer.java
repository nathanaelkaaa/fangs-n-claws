package net.raptorzizi.fangs_n_claws.entity.evil_bat;

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

public class EvilBatRenderer extends GeoEntityRenderer<EvilBatEntity> {

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/evil_bat_eyes.png");

    public EvilBatRenderer(EntityRendererProvider.Context context) {
        super(context, new EvilBatModel());
        this.shadowRadius = 0.2f;
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, EvilBatEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType eyesType = RenderType.eyes(EYES);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay, -1);
            }
        });
    }

    @Override
    protected float getDeathMaxRotation(EvilBatEntity animatable) {
        return 0f;
    }
}
