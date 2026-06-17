package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class DesertScorpionModel extends GeoModel<DesertScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(DesertScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DesertScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/desert_scorpion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DesertScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/scorpion.animation.json");
    }
}
