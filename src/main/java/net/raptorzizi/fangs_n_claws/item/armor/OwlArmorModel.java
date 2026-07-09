package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class OwlArmorModel extends GeoModel<OwlArmorItem> {

    @Override
    public ResourceLocation getModelResource(OwlArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/owl_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OwlArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/models/armor/owl_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OwlArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/owl_armor.animation.json");
    }
}
