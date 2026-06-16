package net.raptorzizi.fangs_n_claws.entity.ice_golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.util.Color;

import java.util.List;
import java.util.Random;

public class IceGolemRenderer extends GeoEntityRenderer<IceGolemEntity> {

    private static final String LEFT_HAND_BONE = "Left Hand";

    public IceGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IceGolemModel());
        this.addRenderLayer(new IceGolemSnowLayer(this));
        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, IceGolemEntity animatable) {
                if (!isFishBone(bone.getName())) return null;
                List<ItemStack> fish = animatable.getFrozenFish();
                if (fish.isEmpty()) return null;
                int boneIdx = fishIndex(bone.getName());
                int[] order = shuffledBoneOrder(animatable);
                for (int slot = 0; slot < fish.size(); slot++) {
                    if (order[slot] == boneIdx) return fish.get(slot);
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack,
                                                                  IceGolemEntity animatable) {
                return ItemDisplayContext.FIXED;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              IceGolemEntity animatable, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                int idx = fishIndex(bone.getName());
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(fishRotation(animatable, idx, 0)));
                poseStack.mulPose(Axis.YP.rotationDegrees(fishRotation(animatable, idx, 1)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(fishRotation(animatable, idx, 2)));
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource,
                        partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public RenderType getRenderType(IceGolemEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }

    @Override
    public Color getRenderColor(IceGolemEntity animatable, float partialTick, int packedLight) {
        return Color.ofRGBA(255, 255, 255, 200);
    }

    @Override
    protected float getDeathMaxRotation(IceGolemEntity animatable) {
        return 0f;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, IceGolemEntity animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, int colour) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender && isFishBone(bone.getName())
                && bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(Sheets.translucentCullBlockSheet());
        }
    }

    private static boolean isFishBone(String name) {
        for (String fn : IceGolemModel.FISH_BONE_NAMES) {
            if (fn.equals(name)) return true;
        }
        return false;
    }

    private static int fishIndex(String name) {
        for (int i = 0; i < IceGolemModel.FISH_BONE_NAMES.length; i++) {
            if (IceGolemModel.FISH_BONE_NAMES[i].equals(name)) return i;
        }
        return 0;
    }

    private static int[] shuffledBoneOrder(IceGolemEntity entity) {
        int n = IceGolemModel.FISH_BONE_NAMES.length;
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Random rng = new Random(entity.getUUID().getLeastSignificantBits());
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        }
        return order;
    }

    private static float fishRotation(IceGolemEntity entity, int idx, int axis) {
        long seed = entity.getUUID().getMostSignificantBits() + (long) idx * 987654L + (long) axis * 111111L;
        return Math.abs(seed) % 360;
    }

    @Override
    public void render(IceGolemEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        BakedGeoModel bakedModel = this.getGeoModel()
                .getBakedModel(this.getGeoModel().getModelResource(entity));
        GeoBone leftHand = null;
        boolean boneWasHidden = false;
        float handScale = entity.getHandScale();
        if (handScale < 1.0f && bakedModel != null) {
            leftHand = bakedModel.getBone(LEFT_HAND_BONE).orElse(null);
            if (leftHand != null) {
                if (handScale <= 0f) {
                    leftHand.setHidden(true);
                    boneWasHidden = true;
                } else {
                    leftHand.setScaleX(handScale);
                    leftHand.setScaleY(handScale);
                    leftHand.setScaleZ(handScale);
                }
            }
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        if (leftHand != null) {
            if (boneWasHidden) leftHand.setHidden(false);
            else { leftHand.setScaleX(1f); leftHand.setScaleY(1f); leftHand.setScaleZ(1f); }
        }
    }
}
