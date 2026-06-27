package net.raptorzizi.fangs_n_claws.entity.horse_bat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class HorseBatModel extends GeoModel<HorseBatEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/horse_bat.png");

    @Override
    public ResourceLocation getModelResource(HorseBatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/horse_mob.json");
    }

    @Override
    public ResourceLocation getTextureResource(HorseBatEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HorseBatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/horse_mob.animation.json");
    }

    @Override
    public void setCustomAnimations(HorseBatEntity animatable, long instanceId, AnimationState<HorseBatEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone neck = getAnimationProcessor().getBone("Neck");
        if (neck != null) {
            neck.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD + (-30f * Mth.DEG_TO_RAD));
            neck.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }

        GeoBone tailA = getAnimationProcessor().getBone("TailA");
        if (tailA != null) {
            int seed = (int)(animatable.getUUID().getLeastSignificantBits() & 0xFF);
            int period = 160 + (seed % 60);
            int duration = 22;
            int phase = (animatable.tickCount + seed * 7) % period;
            tailA.setRotY(phase < duration ? Mth.sin(phase * Mth.PI / duration) * 0.35f : 0f);
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
}
