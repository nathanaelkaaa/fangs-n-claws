package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class HellFirePitchforkItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation BASE_TEXTURE =
            FangsClawsMod.id("textures/entity/fire_pitchfork/hellfire_pitchfork.png");

    private final EntityModelSet modelSet;
    private TridentModel model;

    public HellFirePitchforkItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.modelSet = modelSet;
    }

    private TridentModel model() {
        if (this.model == null) {
            this.model = new TridentModel(this.modelSet.bakeLayer(ModelLayers.TRIDENT));
        }
        return this.model;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer base = ItemRenderer.getFoilBufferDirect(
                bufferSource, model().renderType(BASE_TEXTURE), false, stack.hasFoil());
        model().renderToBuffer(poseStack, base, packedLight, packedOverlay);

        VertexConsumer fire = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FirePitchforkItemRenderer.currentFireTexture()));
        model().renderToBuffer(poseStack, fire, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    static ResourceLocation baseTexture() {
        return BASE_TEXTURE;
    }
}
