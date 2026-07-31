package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.GeoModel;

public class PurpleWormModel extends GeoModel<PurpleWormEntity> {

    private static final float HEAD_YAW_MAX  = 25.0f;
    private static final float PART5_YAW_MAX = 18.0f;
    private static final float PART4_YAW_MAX = 18.0f;
    private static final float HEAD_PITCH_MAX  = 25.0f;
    private static final float PART5_PITCH_MAX = 18.0f;
    private static final float PART4_PITCH_MAX = 18.0f;

    @Override
    public ResourceLocation getModelResource(PurpleWormEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/purple_worm.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PurpleWormEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/purple_worm.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PurpleWormEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/purple_worm.animation.json");
    }

    @Override
    public void setCustomAnimations(PurpleWormEntity entity, long instanceId,
                                    AnimationState<PurpleWormEntity> animationState) {
        CoreGeoBone head  = getAnimationProcessor().getBone("Head");
        if (head == null) return;
        CoreGeoBone part5 = getAnimationProcessor().getBone("part5");
        CoreGeoBone part4 = getAnimationProcessor().getBone("part4");

        float partialTick  = animationState.getPartialTick();
        float desired      = entity.getHeadYawOffset(partialTick);
        float desiredPitch = entity.getHeadPitchOffset(partialTick);

        float headYaw  = Mth.clamp(desired, -HEAD_YAW_MAX, HEAD_YAW_MAX);
        float part5Yaw = Mth.clamp(desired - headYaw, -PART5_YAW_MAX, PART5_YAW_MAX);
        float part4Yaw = Mth.clamp(desired - headYaw - part5Yaw, -PART4_YAW_MAX, PART4_YAW_MAX);

        float headPitch  = Mth.clamp(desiredPitch, -HEAD_PITCH_MAX, HEAD_PITCH_MAX);
        float part5Pitch = Mth.clamp(desiredPitch - headPitch, -PART5_PITCH_MAX, PART5_PITCH_MAX);
        float part4Pitch = Mth.clamp(desiredPitch - headPitch - part5Pitch, -PART4_PITCH_MAX, PART4_PITCH_MAX);

        head.setRotY(head.getRotY() + headYaw * Mth.DEG_TO_RAD);
        head.setRotX(head.getRotX() + headPitch * Mth.DEG_TO_RAD);
        if (part5 != null) {
            part5.setRotY(part5.getRotY() + part5Yaw * Mth.DEG_TO_RAD);
            part5.setRotX(part5.getRotX() + part5Pitch * Mth.DEG_TO_RAD);
        }
        if (part4 != null) {
            part4.setRotY(part4.getRotY() + part4Yaw * Mth.DEG_TO_RAD);
            part4.setRotX(part4.getRotX() + part4Pitch * Mth.DEG_TO_RAD);
        }
    }
}
