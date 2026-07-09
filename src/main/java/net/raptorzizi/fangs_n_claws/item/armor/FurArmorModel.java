package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class FurArmorModel extends GeoModel<FurArmorItem> {

    @Override
    public ResourceLocation getModelResource(FurArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/fur_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FurArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/models/armor/fur_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FurArmorItem animatable) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/fur_armor.animation.json");
    }
}
