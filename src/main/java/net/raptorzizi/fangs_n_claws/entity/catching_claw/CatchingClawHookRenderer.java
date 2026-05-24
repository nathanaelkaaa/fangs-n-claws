package net.raptorzizi.fangs_n_claws.entity.catching_claw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class CatchingClawHookRenderer<T extends CatchingClawHookEntity> extends EntityRenderer<T> {

    private static final ResourceLocation DEFAULT_TEXTURE =
            FangsClawsMod.id("textures/entity/catching_claw_hook.png");

    protected static final float SPRITE_SIZE  = 0.3f;
    protected static final int   LINE_SEGMENTS = 16;

    protected final ResourceLocation texture;

    public CatchingClawHookRenderer(EntityRendererProvider.Context context) {
        this(context, DEFAULT_TEXTURE);
    }

    public CatchingClawHookRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public void render(T entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        renderSprite(entity, partialTick, poseStack, bufferSource, packedLight);

        Entity owner = entity.getOwner();
        if (owner instanceof Player player) {
            renderLine(entity, player, partialTick, poseStack, bufferSource);
        }

        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderSprite(T entity, float partialTick,
                               PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(camRot);
        poseStack.scale(SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

        Matrix4f mat = poseStack.last().pose();

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutout(texture));
        float h = 0.5f;

        vc.vertex(mat, -h,  h, 0).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(),0, 0, 1).endVertex();
        vc.vertex(mat, -h, -h, 0).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(),0, 0, 1).endVertex();
        vc.vertex(mat,  h, -h, 0).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(),0, 0, 1).endVertex();
        vc.vertex(mat,  h,  h, 0).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(),0, 0, 1).endVertex();

        poseStack.popPose();
    }

    private void renderLine(T entity, Player player, float partialTick,
                             PoseStack poseStack, MultiBufferSource bufferSource) {

        double hookX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double hookY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double hookZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        double px = Mth.lerp(partialTick, player.xOld, player.getX());
        double py = Mth.lerp(partialTick, player.yOld, player.getY());
        double pz = Mth.lerp(partialTick, player.zOld, player.getZ());

        float attackAnim  = player.getAttackAnim(partialTick);
        float swingAngle  = Mth.sin(Mth.sqrt(attackAnim) * Mth.PI);

        float bodyYawRad = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot)
                           * (Mth.PI / 180f);
        double sinYaw = Mth.sin(bodyYawRad);
        double cosYaw = Mth.cos(bodyYawRad);

        float handSide = (player.getMainArm() == HumanoidArm.RIGHT) ? 1.0f : -1.0f;

        double handX = px - cosYaw * 0.35 * handSide + sinYaw * 0.8 * swingAngle;
        double handY = py + player.getEyeHeight(player.getPose()) - 0.45 + 0.1 * swingAngle;
        double handZ = pz - sinYaw * 0.35 * handSide - cosYaw * 0.8 * swingAngle;

        double dx = handX - hookX;
        double dy = handY - hookY;
        double dz = handZ - hookZ;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
        Matrix4f mat = poseStack.last().pose();

        float nx = (float) dx;
        float ny = (float) dy;
        float nz = (float) dz;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) { nx /= len; ny /= len; nz /= len; }

        for (int i = 0; i < LINE_SEGMENTS; i++) {
            float t0 = (float)  i      / LINE_SEGMENTS;
            float t1 = (float) (i + 1) / LINE_SEGMENTS;

            float x0 = (float) (dx * t0);
            float y0 = (float) (dy * t0 - 0.3 * Math.sin(Math.PI * t0));
            float z0 = (float) (dz * t0);

            float x1 = (float) (dx * t1);
            float y1 = (float) (dy * t1 - 0.3 * Math.sin(Math.PI * t1));
            float z1 = (float) (dz * t1);

            vc.vertex(mat, x0, y0, z0).color(0, 0, 0, 255).normal(poseStack.last().normal(),nx, ny, nz).endVertex();
            vc.vertex(mat, x1, y1, z1).color(0, 0, 0, 255).normal(poseStack.last().normal(),nx, ny, nz).endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
