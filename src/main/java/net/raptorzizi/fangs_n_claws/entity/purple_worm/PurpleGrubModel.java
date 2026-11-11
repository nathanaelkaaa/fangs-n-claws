package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class PurpleGrubModel extends GeoModel<PurpleGrubEntity> {

    private static final ResourceLocation MODEL     = FangsClawsMod.id("geo/purple_grub.geo.json");
    private static final ResourceLocation TEXTURE   = FangsClawsMod.id("textures/entity/purple_grub.png");
    private static final ResourceLocation ANIMATION = FangsClawsMod.id("animations/purple_grub.animation.json");

    @Override
    public ResourceLocation getModelResource(PurpleGrubEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PurpleGrubEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PurpleGrubEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(PurpleGrubEntity animatable, long instanceId,
                                    AnimationState<PurpleGrubEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head == null) return;

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
