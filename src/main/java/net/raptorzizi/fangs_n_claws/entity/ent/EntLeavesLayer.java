package net.raptorzizi.fangs_n_claws.entity.ent;

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

public class EntLeavesLayer extends GeoRenderLayer<EntEntity> {

    private static final ResourceLocation LEAVES = FangsClawsMod.id("textures/entity/ent_leaves.png");
    private static final int OPAQUE = 0xFF000000;

    public EntLeavesLayer(GeoRenderer<EntEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, EntEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        int foliage = animatable.fixedFoliageColor()
                .orElseGet(() -> BiomeColors.getAverageFoliageColor(animatable.level(), animatable.blockPosition()));
        RenderType leavesType = RenderType.entityCutoutNoCull(LEAVES);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, leavesType,
                bufferSource.getBuffer(leavesType), partialTick, packedLight, packedOverlay,
                OPAQUE | (foliage & 0x00FFFFFF));
    }
}
