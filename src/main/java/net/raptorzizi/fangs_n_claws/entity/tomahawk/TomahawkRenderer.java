package net.raptorzizi.fangs_n_claws.entity.tomahawk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class TomahawkRenderer extends EntityRenderer<TomahawkProjectile> {

    private static final float SPIN_DEGREES_PER_TICK = 36.0F;

    public TomahawkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TomahawkProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        ItemStack item = entity.getItem();
        poseStack.pushPose();

        Vec3 motion = entity.getDeltaMovement();
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (180.0 / Math.PI)) + 90.0F);
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (180.0 / Math.PI)) - 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        float spin = entity.isInGround() ? 0.0F : (entity.tickCount + partialTick) * SPIN_DEGREES_PER_TICK;
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot - spin));

        Minecraft.getInstance().getItemRenderer().renderStatic(
                item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TomahawkProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
