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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
            // renderSingleBlock() forces mipmap=false on the block atlas texture (it's built for
            // items/GUI icons, always crisp at a fixed small size). Used here for a full in-world
            // block, that means the mimicked texture never falls back to lower mip levels at
            // distance -> aliasing/shimmer on high-frequency textures (e.g. Bamboo Mosaic) and
            // wasted fill-rate. Route through the same chunk render type real terrain uses instead,
            // so the ghost block respects mipmapping exactly like the block it mimics would.
            RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(mimic);
            VertexConsumer buffer = bufferSource.getBuffer(renderType);
            dispatcher.renderBatched(mimic, blockEntity.getBlockPos(), level, poseStack, buffer, true, level.random);
        } else {
            // Rare fallback (e.g. a mimicked block entity with animated model, like a chest) :
            // no in-world tesselation path exists for that, so keep the original behavior.
            dispatcher.renderSingleBlock(mimic, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
