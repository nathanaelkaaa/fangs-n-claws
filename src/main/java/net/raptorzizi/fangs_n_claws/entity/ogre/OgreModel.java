package net.raptorzizi.fangs_n_claws.entity.ogre;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class OgreModel extends GeoModel<OgreEntity> {

    @Override
    public ResourceLocation getModelResource(OgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/ogre.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/ogre.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/ogre.animation.json");
    }

    @Override
    public void setCustomAnimations(OgreEntity animatable, long instanceId, AnimationState<OgreEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}