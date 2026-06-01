package net.raptorzizi.fangs_n_claws.entity.cave_ogre;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CaveOgreModel extends GeoModel<CaveOgreEntity> {

    @Override
    public ResourceLocation getModelResource(CaveOgreEntity entity) {
        if (entity.isSiamese())
            return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/cave_ogre_siamese.geo.json");
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/cave_ogre.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CaveOgreEntity entity) {
        if (entity.isSiamese())
            return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/cave_ogre_siamese.png");
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/cave_ogre.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CaveOgreEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/ogre.animation.json");
    }

    @Override
    public void setCustomAnimations(CaveOgreEntity animatable, long instanceId, AnimationState<CaveOgreEntity> animationState) {
        GeoBone head  = getAnimationProcessor().getBone("Head");
        GeoBone head2 = getAnimationProcessor().getBone("Head2");

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float basePitch = entityData.headPitch() * Mth.DEG_TO_RAD;
        float baseYaw   = entityData.netHeadYaw() * Mth.DEG_TO_RAD;

        if (head != null) {
            head.setRotX(basePitch);
            head.setRotY(baseYaw);
        }

        if (head2 != null) {
            float phase    = animatable.getId() * 1.9f;
            float tick     = animatable.tickCount * 0.035f + phase;
            float yawOff   = Mth.sin(tick)                * (22.0f * Mth.DEG_TO_RAD);
            float pitchOff = Mth.sin(tick * 0.7f + 1.0f) * ( 8.0f * Mth.DEG_TO_RAD);
            head2.setRotX(basePitch + pitchOff);
            head2.setRotY(baseYaw   + yawOff);
        }
    }
}
