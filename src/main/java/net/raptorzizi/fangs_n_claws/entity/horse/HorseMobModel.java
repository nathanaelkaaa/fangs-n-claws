package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import net.minecraft.resources.ResourceLocation;

public class HorseMobModel extends GeoModel<HorseMob> {

    private static final float FRONT_LEG_ROT     = Mth.PI / 3f;
    private static final float FRONT_LEG_PAW_AMP = 0.25f;
    private static final float FRONT_LEG_LIFT_Y  = 12f;
    private static final float FRONT_LEG_TUCK_Z  = 2f;

    @Override
    public ResourceLocation getModelResource(HorseMob entity) {
        return FangsClawsMod.id("geo/horse_mob.json");
    }

    @Override
    public ResourceLocation getAnimationResource(HorseMob entity) {
        return FangsClawsMod.id("animations/horse_mob.animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(HorseMob entity) {
        return entity.textureLocation();
    }

    @Override
    public void setCustomAnimations(HorseMob animatable, long instanceId, AnimationState<HorseMob> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone neck = getAnimationProcessor().getBone("Neck");
        if (neck != null) {
            neck.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD + (-30f * Mth.DEG_TO_RAD));
            neck.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }

        GeoBone tailA = getAnimationProcessor().getBone("TailA");
        if (tailA != null) {
            int seed = (int) (animatable.getUUID().getLeastSignificantBits() & 0xFF);
            int period = 160 + (seed % 60);
            int duration = 22;
            int phase = (animatable.tickCount + seed * 7) % period;
            tailA.setRotY(phase < duration ? Mth.sin(phase * Mth.PI / duration) * 0.35f : 0f);
        }

        float stand = animatable.isStanding() ? animatable.getStandAnim(animationState.getPartialTick()) : 0f;
        if (stand > 0.001f) {
            float age = animatable.tickCount + animationState.getPartialTick();
            float paw = Mth.cos(age * 0.8f + Mth.PI);

            blendRotX("Body",  stand, Mth.PI / 4f);
            rearFrontLeg("Leg4A", stand, FRONT_LEG_ROT + paw * FRONT_LEG_PAW_AMP);
            rearFrontLeg("Leg3A", stand, FRONT_LEG_ROT - paw * FRONT_LEG_PAW_AMP);
            blendRotX("Leg2A", stand, 0.2618f);
            blendRotX("Leg1A", stand, 0.2618f);
        }

        boolean unsaddled = !animatable.isSaddled();
        setHidden("Saddle", unsaddled);
        setHidden("HeadSaddle", unsaddled);
        setHidden("SaddleMouthL", unsaddled);
        setHidden("SaddleMouthR", unsaddled);
        setHidden("SaddleMouthLine", unsaddled);
        setHidden("SaddleMouthLineR", unsaddled);
        setHidden("Bag1", true);
        setHidden("Bag2", true);
    }

    private void setHidden(String bone, boolean hidden) {
        GeoBone b = getAnimationProcessor().getBone(bone);
        if (b != null) b.setHidden(hidden);
    }

    private void blendRotX(String bone, float t, float target) {
        GeoBone b = getAnimationProcessor().getBone(bone);
        if (b != null) b.setRotX(Mth.lerp(t, b.getRotX(), target));
    }

    private void rearFrontLeg(String bone, float t, float rotX) {
        GeoBone leg = getAnimationProcessor().getBone(bone);
        if (leg == null) return;
        leg.setRotX(Mth.lerp(t, leg.getRotX(), rotX));
        leg.setPosY(t * FRONT_LEG_LIFT_Y);
        leg.setPosZ(t * FRONT_LEG_TUCK_Z);
    }
}
