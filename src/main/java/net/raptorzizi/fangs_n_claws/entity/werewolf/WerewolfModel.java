package net.raptorzizi.fangs_n_claws.entity.werewolf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class WerewolfModel extends GeoModel<WerewolfEntity> {

    @Override
    public ResourceLocation getModelResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/werewolf.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/werewolf.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/werewolf.animation.json");
    }

    @Override
    public void setCustomAnimations(WerewolfEntity animatable, long instanceId, AnimationState<WerewolfEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            float runPitchOffset = animatable.isRunning() ? 30.0f : 0.0f;
            head.setRotX((entityData.headPitch() + runPitchOffset) * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
