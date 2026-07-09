package net.raptorzizi.fangs_n_claws.entity.ice_golem;

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

public class IceGolemSnowLayer extends GeoRenderLayer<IceGolemEntity> {

    private static final ResourceLocation SNOW_TEXTURE =
            FangsClawsMod.id("textures/entity/ice_golem_snow.png");

    public IceGolemSnowLayer(GeoRenderer<IceGolemEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, IceGolemEntity animatable, BakedGeoModel bakedModel,
                       @Nullable RenderType renderType, MultiBufferSource bufferSource,
                       @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType snowType   = RenderType.entityCutoutNoCull(SNOW_TEXTURE);
        VertexConsumer snowBuf = bufferSource.getBuffer(snowType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                snowType, snowBuf, partialTick, packedLight, packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}
