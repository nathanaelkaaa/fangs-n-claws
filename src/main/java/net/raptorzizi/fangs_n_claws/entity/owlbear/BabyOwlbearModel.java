package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class BabyOwlbearModel extends GeoModel<BabyOwlbearEntity> {

    @Override
    public ResourceLocation getModelResource(BabyOwlbearEntity entity) {
        return FangsClawsMod.id("geo/baby_owlbear.json");
    }

    @Override
    public ResourceLocation getTextureResource(BabyOwlbearEntity entity) {
        String suffix = entity.isSleepPose() ? "_sleep" : "";
        return FangsClawsMod.id("textures/entity/" + entity.textureBaseName() + suffix + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BabyOwlbearEntity entity) {
        return FangsClawsMod.id("animations/baby_owlbear.animation.json");
    }
}
