package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class CarnivorousPlantSproutModel extends GeoModel<CarnivorousPlantSproutEntity> {

    private static final ResourceLocation MODEL     = FangsClawsMod.id("geo/carnivorous_plant_sprout.geo.json");
    private static final ResourceLocation TEXTURE   = FangsClawsMod.id("textures/entity/carnivorous_plant_sprout.png");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/carnivorous_plant_sprout.animation.json");

    @Override
    public ResourceLocation getModelResource(CarnivorousPlantSproutEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CarnivorousPlantSproutEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CarnivorousPlantSproutEntity entity) {
        return ANIMATION;
    }
}
