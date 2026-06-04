package net.raptorzizi.fangs_n_claws.entity.hell_ogre;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class HellOgreModel extends GeoModel<HellOgreEntity> {

    @Override
    public ResourceLocation getModelResource(HellOgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/hell_ogre.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HellOgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/hell_ogre.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HellOgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/hell_ogre.animation.json");
    }

    @Override
    public void setCustomAnimations(HellOgreEntity animatable, long instanceId, AnimationState<HellOgreEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float basePitch = entityData.headPitch() * Mth.DEG_TO_RAD;
        float baseYaw   = entityData.netHeadYaw() * Mth.DEG_TO_RAD;

        if (head != null) {
            head.setRotX(basePitch);
            head.setRotY(baseYaw);
        }
    }
}
