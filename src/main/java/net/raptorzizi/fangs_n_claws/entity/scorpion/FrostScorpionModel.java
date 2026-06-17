package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class FrostScorpionModel extends GeoModel<FrostScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(FrostScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrostScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/frost_scorpion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FrostScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/scorpion.animation.json");
    }
}
