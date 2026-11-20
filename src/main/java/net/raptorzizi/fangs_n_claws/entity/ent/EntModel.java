package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class EntModel extends GeoModel<EntEntity> {

    private static final ResourceLocation MODEL = FangsClawsMod.id("geo/ent.geo.json");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/ent.animation.json");

    @Override
    public ResourceLocation getModelResource(EntEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EntEntity entity) {
        return entity.textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(EntEntity entity) {
        return ANIMATION;
    }
}
