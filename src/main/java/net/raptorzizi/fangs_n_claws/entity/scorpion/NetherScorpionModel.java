package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class NetherScorpionModel extends GeoModel<NetherScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(NetherScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "geo/scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetherScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/entity/nether_scorpion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetherScorpionEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "animations/scorpion.animation.json");
    }
}
