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

public class PoisonSplitRenderer extends EntityRenderer<PoisonSplitProjectile> {

    private static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/poison_split.png");

    private static final float RENDER_SCALE = 3.0f;

    private final PoisonSplitModel model;

    public PoisonSplitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new PoisonSplitModel(ctx.bakeLayer(PoisonSplitModel.LAYER));
    }

    @Override
    public void render(@NotNull PoisonSplitProjectile entity, float yaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        float lerpYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw - 180.0F));
        float lerpPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(lerpPitch));
        poseStack.scale(RENDER_SCALE, RENDER_SCALE, RENDER_SCALE);
        poseStack.translate(0.0, 0.3F, 0.0);

        VertexConsumer vc = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PoisonSplitProjectile entity) {
        return TEXTURE;
    }
}
