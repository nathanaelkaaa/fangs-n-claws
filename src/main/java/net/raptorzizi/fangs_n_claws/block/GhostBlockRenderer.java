package net.raptorzizi.fangs_n_claws.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
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

        Minecraft.getInstance().getBlockRenderer()
                .renderSingleBlock(mimic, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
