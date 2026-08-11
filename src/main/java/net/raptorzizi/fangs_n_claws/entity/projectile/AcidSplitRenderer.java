package net.raptorzizi.fangs_n_claws.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.jetbrains.annotations.NotNull;

public class AcidSplitRenderer extends EntityRenderer<AcidSplitProjectile> {

    private static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/acid_split.png");

    private static final float RENDER_SCALE = 3.0f;

    private final AcidSplitModel model;

    public AcidSplitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new AcidSplitModel(ctx.bakeLayer(AcidSplitModel.LAYER));
    }

    @Override
    public void render(@NotNull AcidSplitProjectile entity, float yaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        float lerpYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw - 180.0F));
        float lerpPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(lerpPitch));
        float s = RENDER_SCALE * entity.getScale();
        poseStack.scale(s, s, s);
        poseStack.translate(0.0, 0.3F, 0.0);

        VertexConsumer vc = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AcidSplitProjectile entity) {
        return TEXTURE;
    }
}
