package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class OwlArmorModel extends GeoModel<OwlArmorItem> {

    @Override
    public ResourceLocation getModelResource(OwlArmorItem animatable) {
        return FangsClawsMod.id("geo/owl_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OwlArmorItem animatable) {
        return FangsClawsMod.id("textures/models/armor/owl_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OwlArmorItem animatable) {
        return FangsClawsMod.id("animations/owl_armor.animation.json");
    }
}
