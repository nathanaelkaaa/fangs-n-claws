package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class BabyScorpionModel extends GeoModel<BabyScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(BabyScorpionEntity entity) {
        return FangsClawsMod.id("geo/baby_scorpion.json");
    }

    @Override
    public ResourceLocation getTextureResource(BabyScorpionEntity entity) {
        return entity.textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(BabyScorpionEntity entity) {
        return FangsClawsMod.id("animations/baby_scorpion.animation.json");
    }
}
