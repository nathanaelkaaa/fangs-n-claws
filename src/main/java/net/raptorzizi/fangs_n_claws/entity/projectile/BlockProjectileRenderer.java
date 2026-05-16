package net.raptorzizi.fangs_n_claws.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BlockProjectileRenderer extends EntityRenderer<BlockProjectile> {

    // Spawn

    public BlockProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Animation, Texture

    @Override
    public void render(BlockProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        if (!entity.isRegenMode()) {
            poseStack.scale(1.5f, 1.5f, 1.5f);
        }

        float t       = entity.tickCount + partialTick;
        float speedY  = entity.isRegenMode() ? 6f  : 14f;
        float speedX  = entity.isRegenMode() ? 4f  :  9f;
        poseStack.mulPose(Axis.YP.rotationDegrees(t * speedY));
        poseStack.mulPose(Axis.XP.rotationDegrees(t * speedX));
        poseStack.translate(-0.5, -0.5, -0.5);

        BlockState state = entity.getBlockState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(state, poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockProjectile entity) {
        return ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    }
}
