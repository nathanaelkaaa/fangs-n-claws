package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class PurpleWormArmModel extends GeoModel<PurpleWormArmEntity> {

    @Override
    public ResourceLocation getModelResource(PurpleWormArmEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/purple_worm_arm.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PurpleWormArmEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/purple_worm_arm.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PurpleWormArmEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/purple_worm_arm.animation.json");
    }
}
