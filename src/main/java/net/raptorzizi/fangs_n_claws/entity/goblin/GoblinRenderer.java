package net.raptorzizi.fangs_n_claws.entity.goblin;

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

public class GoblinRenderer extends GeoEntityRenderer<GoblinEntity> {

    public GoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinModel());
        this.shadowRadius = 0.3f;

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {

            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, GoblinEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem" -> animatable.getItemBySlot(EquipmentSlot.MAINHAND);
                    case "leftItem"  -> animatable.getItemBySlot(EquipmentSlot.OFFHAND);
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, GoblinEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem" -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    case "leftItem"  -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            @Override
            public void renderForBone(PoseStack poseStack, GoblinEntity animatable, GeoBone bone,
                                         RenderType renderType, MultiBufferSource bufferSource,
                                         VertexConsumer buffer, float partialTick,
                                         int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(0, 0.1, 0.1);
                poseStack.scale(0.7f, 0.7f, 0.7f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });
    }
}
