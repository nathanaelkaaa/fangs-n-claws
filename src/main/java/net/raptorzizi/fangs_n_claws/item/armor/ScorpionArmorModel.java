package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class ScorpionArmorModel extends GeoModel<ScorpionArmorItem> {

    @Override
    public ResourceLocation getModelResource(ScorpionArmorItem animatable) {
        return FangsClawsMod.id("geo/scorpion_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScorpionArmorItem animatable) {
        return FangsClawsMod.id("textures/models/armor/scorpion_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScorpionArmorItem animatable) {
        return FangsClawsMod.id("animations/scorpion_armor.animation.json");
    }
}
