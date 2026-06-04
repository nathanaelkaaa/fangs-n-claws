package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DartGoblinModel extends GeoModel<DartGoblinEntity> {

    @Override
    public RenderType getRenderType(DartGoblinEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public ResourceLocation getModelResource(DartGoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "geo/dart_goblin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DartGoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "textures/entity/dart_goblin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DartGoblinEntity entity) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, "animations/dart_goblin.animation.json");
    }

    @Override
    public void setCustomAnimations(DartGoblinEntity animatable, long instanceId, AnimationState<DartGoblinEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yawRad   = entityData.netHeadYaw() * Mth.DEG_TO_RAD;
        float pitchRad = entityData.headPitch()  * Mth.DEG_TO_RAD;

        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotX(pitchRad);
            head.setRotY(yawRad);
        }
    }
}
