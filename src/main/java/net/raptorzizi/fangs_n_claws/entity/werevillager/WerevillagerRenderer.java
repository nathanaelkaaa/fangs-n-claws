package net.raptorzizi.fangs_n_claws.entity.werevillager;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class WerevillagerRenderer extends MobRenderer<WerevillagerEntity, VillagerModel<WerevillagerEntity>> {

    private static final ResourceLocation BASE_TEXTURE =
            FangsClawsMod.id("textures/entity/werevillager/werevillager.png");

    public WerevillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);

        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               WerevillagerEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                ResourceLocation texture = FangsClawsMod.id(
                        "textures/entity/werevillager/" + entity.getBiomeFolder() + ".png");
                getParentModel().renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.entityCutoutNoCull(texture)),
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(WerevillagerEntity entity) {
        return BASE_TEXTURE;
    }
}
