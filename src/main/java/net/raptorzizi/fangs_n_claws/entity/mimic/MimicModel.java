package net.raptorzizi.fangs_n_claws.entity.mimic;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.model.GeoModel;

public class MimicModel extends GeoModel<MimicEntity> {

    private static final ResourceLocation CHEST_TEXTURE =
            FangsClawsMod.id("textures/entity/mimic_chest.png");

    @Override
    public RenderType getRenderType(MimicEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(MimicEntity entity) {
        return FangsClawsMod.id("geo/mimic.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MimicEntity entity) {
        return CHEST_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MimicEntity entity) {
        return FangsClawsMod.id("animations/mimic.animation.json");
    }
}
