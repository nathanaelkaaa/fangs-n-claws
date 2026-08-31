package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BabyWildWolfModel extends GeoModel<BabyWildWolfEntity> {

    @Override
    public ResourceLocation getModelResource(BabyWildWolfEntity entity) {
        return FangsClawsMod.id("geo/" + entity.geoName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BabyWildWolfEntity entity) {
        return FangsClawsMod.id("textures/entity/" + entity.textureBaseName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BabyWildWolfEntity entity) {
        return FangsClawsMod.id("animations/" + entity.animationName() + ".animation.json");
    }

    @Override
    public void setCustomAnimations(BabyWildWolfEntity animatable, long instanceId,
                                    AnimationState<BabyWildWolfEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head == null) return;
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
