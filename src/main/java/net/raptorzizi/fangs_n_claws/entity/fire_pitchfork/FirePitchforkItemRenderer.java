package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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

public class FirePitchforkItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation BASE_TEXTURE =
            FangsClawsMod.id("textures/entity/fire_pitchfork/fire_pitchfork.png");

    private static final int FRAME_COUNT = 4;
    private static final int FRAME_TICKS = 10;
    private static final ResourceLocation[] FIRE_FRAMES = new ResourceLocation[FRAME_COUNT];
    static {
        for (int i = 0; i < FRAME_COUNT; i++)
            FIRE_FRAMES[i] = FangsClawsMod.id("textures/entity/fire_pitchfork/fire_pitchfork_fire_" + i + ".png");
    }

    private final EntityModelSet modelSet;
    private TridentModel model;

    public FirePitchforkItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
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

        VertexConsumer fire = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(currentFireTexture()));
        model().renderToBuffer(poseStack, fire, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    static ResourceLocation currentFireTexture() {
        Minecraft mc = Minecraft.getInstance();
        long time = mc.level != null ? mc.level.getGameTime() : 0L;
        return FIRE_FRAMES[(int) ((time / FRAME_TICKS) % FRAME_COUNT)];
    }

    static ResourceLocation baseTexture() {
        return BASE_TEXTURE;
    }
}
