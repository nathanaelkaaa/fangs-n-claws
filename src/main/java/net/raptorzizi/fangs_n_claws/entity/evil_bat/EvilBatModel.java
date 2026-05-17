package net.raptorzizi.fangs_n_claws.entity.evil_bat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class EvilBatModel extends GeoModel<EvilBatEntity> {

    @Override
    public RenderType getRenderType(EvilBatEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(EvilBatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/evil_bat.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EvilBatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/evil_bat.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EvilBatEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/evil_bat.animation.json");
    }

    @Override
    public void setCustomAnimations(EvilBatEntity animatable, long instanceId, AnimationState<EvilBatEntity> animationState) {
        GeoBone body = getAnimationProcessor().getBone("body");
        GeoBone head = getAnimationProcessor().getBone("Head");
        if (body != null) {body.setRotZ(0f);}
        if (head != null) {head.setRotZ(0f);}
        if (animatable.isResting()) return;

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float pitchRad = entityData.headPitch() * Mth.DEG_TO_RAD;

        if (body != null) {body.setRotX(pitchRad);}
        if (head != null) {head.setRotX(pitchRad);}
    }
}
