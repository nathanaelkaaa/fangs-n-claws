package net.raptorzizi.fangs_n_claws.entity.hyena;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import com.mojang.math.Axis;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HyenaRenderer extends GeoEntityRenderer<HyenaEntity> {

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/hyena_eyes.png");
    private static final ResourceLocation TAME_EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/hyena_tame_eyes.png");

    private static final float HELD_X = 0.0F, HELD_Y = -0.05F, HELD_Z = -0.20F;
    private static final float HELD_SCALE = 1F;

    private static final ResourceLocation COLLAR =
            FangsClawsMod.id("textures/entity/hyena_collar.png");

    public HyenaRenderer(EntityRendererProvider.Context context) {
        super(context, new HyenaModel());
        this.shadowRadius = 0.5F;

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, HyenaEntity animatable) {
                return "Lower Jaw".equals(bone.getName())
                        ? animatable.getItemBySlot(EquipmentSlot.MAINHAND) : null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack,
                                                                  HyenaEntity animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              HyenaEntity animatable, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(HELD_X, HELD_Y, HELD_Z);
                poseStack.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource,
                        partialTick, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });

        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, HyenaEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                boolean tamed = animatable.isTamed();
                RenderType eyesType = tamed
                        ? RenderType.entityCutoutNoCull(TAME_EYES)
                        : RenderType.eyes(EYES);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick,
                        tamed ? packedLight : LightTexture.FULL_SKY, packedOverlay, -1);
            }
        });
    
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, HyenaEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (!animatable.isTamed() || animatable.isInvisible()) return;
                RenderType collarType = RenderType.entityCutoutNoCull(COLLAR);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, collarType,
                        bufferSource.getBuffer(collarType), partialTick, packedLight, packedOverlay,
                        animatable.getCollarColor().getTextureDiffuseColor());
            }
        });
}

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    HyenaEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        float s = animatable.isLeader() ? 1.1F : 1.0F;
        super.scaleModelForRender(widthScale * s, heightScale * s, poseStack,
                animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
