package net.raptorzizi.fangs_n_claws.entity.skull;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class SkullModel extends GeoModel<SkullEntity> {

    private static final ResourceLocation MODEL     = FangsClawsMod.id("geo/skull.geo.json");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/skull.animation.json");

    @Override
    public ResourceLocation getModelResource(SkullEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SkullEntity entity) {
        return FangsClawsMod.id("textures/entity/" + entity.textureBaseName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SkullEntity entity) {
        return ANIMATION;
    }
}
