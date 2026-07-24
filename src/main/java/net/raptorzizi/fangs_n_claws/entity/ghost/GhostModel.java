package net.raptorzizi.fangs_n_claws.entity.ghost;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GhostModel extends GeoModel<GhostEntity> {

    private static final ResourceLocation NORMAL_TEXTURE =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/ghost.png");
    private static final ResourceLocation ANGRY_TEXTURE  =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/angry_ghost.png");

    @Override
    public RenderType getRenderType(GhostEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public ResourceLocation getModelResource(GhostEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GhostEntity entity) {
        return entity.isAngry() ? ANGRY_TEXTURE : NORMAL_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GhostEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/ghost.animation.json");
    }

    @Override
    public void setCustomAnimations(GhostEntity animatable, long instanceId, AnimationState<GhostEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitchRad = entityData.headPitch() * Mth.DEG_TO_RAD;
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
