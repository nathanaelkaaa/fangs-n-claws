package net.raptorzizi.fangs_n_claws.entity.frozen_box;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class FrozenBoxModel extends GeoModel<FrozenBoxEntity> {

    @Override
    public ResourceLocation getModelResource(FrozenBoxEntity entity) {
        return FangsClawsMod.id("geo/frozen_box.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrozenBoxEntity entity) {
        return FangsClawsMod.id("textures/entity/frozen.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FrozenBoxEntity entity) {
        return FangsClawsMod.id("animations/frozen_box.animation.json");
    }
}
