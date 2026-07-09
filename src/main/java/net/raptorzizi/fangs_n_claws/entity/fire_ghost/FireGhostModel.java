package net.raptorzizi.fangs_n_claws.entity.fire_ghost;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class FireGhostModel extends GeoModel<FireGhostEntity> {

    private static final ResourceLocation NORMAL_TEXTURE =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/fire_ghost.png");
    private static final ResourceLocation ANGRY_TEXTURE =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/angry_fire_ghost.png");

    @Override
    public RenderType getRenderType(FireGhostEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(FireGhostEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/fire_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FireGhostEntity entity) {
        return entity.isAngry() ? ANGRY_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FireGhostEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/ghost.animation.json");
    }

    @Override
    public void setCustomAnimations(FireGhostEntity animatable, long instanceId, AnimationState<FireGhostEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitchRad  = entityData.headPitch() * Mth.DEG_TO_RAD;
        float speed     = (float) animatable.getDeltaMovement().horizontalDistance();
        float amplitude = Mth.clamp(speed * 3.0f, 0.0f, 0.18f);
        float bob       = Mth.sin(animatable.tickCount * 0.14f) * amplitude;

        var body = getAnimationProcessor().getBone("body");
        if (body != null) {
            body.setRotX(pitchRad);
            body.setRotZ(0f);
            body.setPosY(body.getPosY() + bob);
        }
    }
}
