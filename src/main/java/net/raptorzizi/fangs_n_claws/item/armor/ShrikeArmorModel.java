package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class ShrikeArmorModel extends GeoModel<ShrikeArmorItem> {

    @Override
    public ResourceLocation getModelResource(ShrikeArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/owl_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShrikeArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/models/armor/shrike_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShrikeArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/owl_armor.animation.json");
    }
}
