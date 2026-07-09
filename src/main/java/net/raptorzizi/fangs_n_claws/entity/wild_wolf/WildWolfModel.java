package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class WildWolfModel extends GeoModel<WildWolfEntity> {

    private static final ResourceLocation TEX_BLACK     = tex("wild_wolf");
    private static final ResourceLocation TEX_BROWN     = tex("wild_wolf_brown");
    private static final ResourceLocation TEX_DARK_GRAY = tex("wild_wolf_dark_gray");
    private static final ResourceLocation TEX_GRAY      = tex("wild_wolf_gray");
    private static final ResourceLocation TEX_WHITE     = tex("wild_wolf_white");

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getModelResource(WildWolfEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/wild_wolf.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WildWolfEntity entity) {
        return switch (entity.getVariant()) {
            case WildWolfEntity.VARIANT_BROWN     -> TEX_BROWN;
            case WildWolfEntity.VARIANT_DARK_GRAY -> TEX_DARK_GRAY;
            case WildWolfEntity.VARIANT_GRAY      -> TEX_GRAY;
            case WildWolfEntity.VARIANT_WHITE     -> TEX_WHITE;
            default                               -> TEX_BLACK;
        };
    }

    @Override
    public ResourceLocation getAnimationResource(WildWolfEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/wild_wolf.animation.json");
    }

    @Override
    public void setCustomAnimations(WildWolfEntity animatable, long instanceId, AnimationState<WildWolfEntity> animationState) {
        var head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
