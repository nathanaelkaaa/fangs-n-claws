package net.raptorzizi.fangs_n_claws.entity.imp;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ImpModel extends GeoModel<ImpEntity> {

    @Override
    public RenderType getRenderType(ImpEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(ImpEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/imp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ImpEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/imp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ImpEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/imp.animation.json");
    }

    @Override
    public void setCustomAnimations(ImpEntity animatable, long instanceId, AnimationState<ImpEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yawRad   = entityData.netHeadYaw() * Mth.DEG_TO_RAD;
        float pitchRad = entityData.headPitch()  * Mth.DEG_TO_RAD;

        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotX(pitchRad);
            head.setRotY(yawRad);
        }
    }
}
