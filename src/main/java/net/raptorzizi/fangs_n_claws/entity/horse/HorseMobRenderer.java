package net.raptorzizi.fangs_n_claws.entity.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HorseMobRenderer extends GeoEntityRenderer<HorseMob> {

    public HorseMobRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseMobModel());
        this.shadowRadius = 0.7F;

        this.addRenderLayer(new HorseArmorLayer<>(this));

        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, HorseMob animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick,
                               int packedLight, int packedOverlay) {
                ResourceLocation eyes = animatable.eyesTexture();
                if (eyes == null) return;
                RenderType eyesType = RenderType.eyes(eyes);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay,
                        1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }
}
