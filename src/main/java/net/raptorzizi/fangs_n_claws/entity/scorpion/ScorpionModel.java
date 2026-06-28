package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class ScorpionModel extends GeoModel<ScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(ScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScorpionEntity entity) {
        return entity.textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(ScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/scorpion.animation.json");
    }
}
