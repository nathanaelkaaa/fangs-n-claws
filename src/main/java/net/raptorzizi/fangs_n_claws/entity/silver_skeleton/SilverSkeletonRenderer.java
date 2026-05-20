package net.raptorzizi.fangs_n_claws.entity.silver_skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class SilverSkeletonRenderer extends GeoEntityRenderer<SilverSkeletonEntity> {

    // Spawn, Animation

    public SilverSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverSkeletonModel());

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {

            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, SilverSkeletonEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem" -> animatable.getItemBySlot(EquipmentSlot.MAINHAND);
                    case "leftItem"  -> animatable.getItemBySlot(EquipmentSlot.OFFHAND);
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, SilverSkeletonEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem", "leftItem" -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            @Override
            public void renderForBone(PoseStack poseStack, SilverSkeletonEntity animatable, GeoBone bone,
                                      RenderType renderType, MultiBufferSource bufferSource,
                                      VertexConsumer buffer, float partialTick,
                                      int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(0, 0.6, 0.7);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });
    }
}
