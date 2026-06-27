package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class NightmareHorseModel extends GeoModel<NightmareHorseEntity> {

    private static final float FRONT_LEG_ROT     = Mth.PI / 3f;
    private static final float FRONT_LEG_PAW_AMP = 0.25f;
    private static final float FRONT_LEG_LIFT_Y  = 12f;
    private static final float FRONT_LEG_TUCK_Z  = 2f;
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/horse_mob.json");
    private static final ResourceLocation ANIM =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/horse_mob.animation.json");
    private static final ResourceLocation TEX_RED =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/nightmare_horse.png");
    private static final ResourceLocation TEX_BLACK =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/nightmare_horse_black.png");

    @Override
    public ResourceLocation getModelResource(NightmareHorseEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareHorseEntity entity) {
        return ANIM;
    }

    @Override
    public ResourceLocation getTextureResource(NightmareHorseEntity entity) {
        return entity.getVariant() == NightmareHorseEntity.VARIANT_BLACK ? TEX_BLACK : TEX_RED;
    }

    @Override
    public void setCustomAnimations(NightmareHorseEntity animatable, long instanceId, AnimationState<NightmareHorseEntity> animationState) {
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
            blendRotX("Leg2A", stand,  0.2618f);
            blendRotX("Leg1A", stand,  0.2618f);
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
