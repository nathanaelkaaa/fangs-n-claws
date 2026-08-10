package net.raptorzizi.fangs_n_claws.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhostBlockRenderer implements BlockEntityRenderer<GhostBlockEntity> {

    public GhostBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GhostBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        BlockState current = level.getBlockState(blockEntity.getBlockPos());
        if (!current.hasProperty(GhostBlock.HAS_MIMIC) || !current.getValue(GhostBlock.HAS_MIMIC)) return;

        BlockState mimic = blockEntity.getMimickedState();
        if (mimic == null) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        if (mimic.getRenderShape() == RenderShape.MODEL) {
            RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(mimic);
            VertexConsumer buffer = bufferSource.getBuffer(renderType);
            dispatcher.renderBatched(mimic, blockEntity.getBlockPos(), level, poseStack, buffer, true, level.random);
        } else {
            dispatcher.renderSingleBlock(mimic, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
