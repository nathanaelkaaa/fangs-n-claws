package net.raptorzizi.fangs_n_claws.entity.hyena;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class HyenaModel extends GeoModel<HyenaEntity> {

    private static final ResourceLocation MODEL     = FangsClawsMod.id("geo/hyena.geo.json");
    private static final ResourceLocation TEXTURE   = FangsClawsMod.id("textures/entity/hyena.png");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/wild_wolf.animation.json");

    @Override
    public ResourceLocation getModelResource(HyenaEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HyenaEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HyenaEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(HyenaEntity animatable, long instanceId, AnimationState<HyenaEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
