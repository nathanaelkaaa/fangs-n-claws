package net.raptorzizi.fangs_n_claws.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GolemGrassLayer extends GeoRenderLayer<GolemEntity> {

    private static final ResourceLocation GRASS_TEXTURE =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/golem_grass.png");

    public GolemGrassLayer(GeoRenderer<GolemEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, GolemEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        int grassColor = BiomeColors.getAverageGrassColor(animatable.level(), animatable.blockPosition());

        float r = ((grassColor >> 16) & 0xFF) / 255.0f;
        float g = ((grassColor >> 8)  & 0xFF) / 255.0f;
        float b = ( grassColor        & 0xFF) / 255.0f;

        RenderType     grassType   = RenderType.entityCutoutNoCull(GRASS_TEXTURE);
        VertexConsumer grassBuffer = bufferSource.getBuffer(grassType);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                grassType, grassBuffer, partialTick, packedLight, packedOverlay,
                r, g, b, 1.0f);
    }
}
