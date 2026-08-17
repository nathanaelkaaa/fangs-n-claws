package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class CarnivorousPlantModel extends GeoModel<CarnivorousPlantEntity> {

    private static final ResourceLocation MODEL     = FangsClawsMod.id("geo/carnivorous_plant.geo.json");
    private static final ResourceLocation TEXTURE   = FangsClawsMod.id("textures/entity/carnivorous_plant.png");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/carnivorous_plant.animation.json");

    private static final float BODY_YAW_FIX = 90.0f;

    @Override
    public ResourceLocation getModelResource(CarnivorousPlantEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CarnivorousPlantEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CarnivorousPlantEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(CarnivorousPlantEntity entity, long instanceId,
                                    AnimationState<CarnivorousPlantEntity> animationState) {
        GeoBone body = getAnimationProcessor().getBone("Body");
        if (body == null) return;
        float base = body.getInitialSnapshot().getRotY();
        float yaw  = entity.getBodyYawOffset(animationState.getPartialTick()) + BODY_YAW_FIX;
        body.setRotY(base + yaw * Mth.DEG_TO_RAD);
    }
}
