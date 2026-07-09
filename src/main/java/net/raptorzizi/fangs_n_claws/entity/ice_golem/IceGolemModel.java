package net.raptorzizi.fangs_n_claws.entity.ice_golem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class IceGolemModel extends GeoModel<IceGolemEntity> {

    @Override
    public ResourceLocation getModelResource(IceGolemEntity entity) {
        return FangsClawsMod.id("geo/ice_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IceGolemEntity entity) {
        return FangsClawsMod.id("textures/entity/ice_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IceGolemEntity entity) {
        return FangsClawsMod.id("animations/ice_golem.animation.json");
    }

    static final String[] FISH_BONE_NAMES = {"fish1","fish2","fish3","fish4","fish5","fish6","fish7"};

    @Override
    public void setCustomAnimations(IceGolemEntity animatable, long instanceId,
                                    AnimationState<IceGolemEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
        for (String name : FISH_BONE_NAMES) {
            CoreGeoBone bone = getAnimationProcessor().getBone(name);
            if (bone instanceof GeoBone geoBone) geoBone.setTrackingMatrices(true);
        }
    }
}
