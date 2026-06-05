package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HellFirePitchforkRenderer extends EntityRenderer<HellFirePitchforkEntity> {

    private final TridentModel model;

    public HellFirePitchforkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TridentModel(context.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void render(HellFirePitchforkEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));

        VertexConsumer base = bufferSource.getBuffer(this.model.renderType(HellFirePitchforkItemRenderer.baseTexture()));
        this.model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        VertexConsumer fire = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FirePitchforkItemRenderer.currentFireTexture()));
        this.model.renderToBuffer(poseStack, fire, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HellFirePitchforkEntity entity) {
        return HellFirePitchforkItemRenderer.baseTexture();
    }
}
