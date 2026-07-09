package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.GeoModel;

public class ScorpionModel extends GeoModel<ScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(ScorpionEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/scorpion.geo.json");
    }

    @Override
    public void setCustomAnimations(ScorpionEntity animatable, long instanceId, AnimationState<ScorpionEntity> state) {
        CoreGeoBone saddle = getAnimationProcessor().getBone("Saddle");
        if (saddle != null) saddle.setHidden(!animatable.isSaddled());
    }

    @Override
    public ResourceLocation getTextureResource(ScorpionEntity entity) {
        return entity.textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(ScorpionEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/scorpion.animation.json");
    }
}
