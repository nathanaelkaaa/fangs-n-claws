package net.raptorzizi.fangs_n_claws.entity.silver_skeleton;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;


public class SilverSkeletonModel extends GeoModel<SilverSkeletonEntity> {

    @Override
    public ResourceLocation getModelResource(SilverSkeletonEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/silver_skeleton.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SilverSkeletonEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/silver_skeleton.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SilverSkeletonEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/silver_skeleton.animation.json");
    }

    @Override
    public void setCustomAnimations(SilverSkeletonEntity entity, long instanceId, AnimationState<SilverSkeletonEntity> animationState) {
        if (animationState == null) return;

        GeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
