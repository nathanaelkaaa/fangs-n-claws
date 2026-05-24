package net.raptorzizi.fangs_n_claws.entity.goblin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GoblinModel extends GeoModel<GoblinEntity> {

    @Override
    public RenderType getRenderType(GoblinEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(GoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/goblin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/goblin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/goblin.animation.json");
    }

    @Override
    public void setCustomAnimations(GoblinEntity animatable, long instanceId, AnimationState<GoblinEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yawRad   = entityData.netHeadYaw()  * Mth.DEG_TO_RAD;
        float pitchRad = entityData.headPitch()    * Mth.DEG_TO_RAD;

        var head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotX(pitchRad);
            head.setRotY(yawRad);
        }
    }
}
