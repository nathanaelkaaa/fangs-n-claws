package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GolemModel extends GeoModel<GolemEntity> {

    @Override
    public ResourceLocation getModelResource(GolemEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GolemEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GolemEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/golem.animation.json");
    }

    @Override
    public void setCustomAnimations(GolemEntity animatable, long instanceId,
                                    AnimationState<GolemEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
