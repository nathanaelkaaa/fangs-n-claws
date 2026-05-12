package net.raptorzizi.fangs_n_claws.entity.ogre;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OgreRenderer extends GeoEntityRenderer<OgreEntity> {

    private static final float SCALE = 1.1F;

    public OgreRenderer(EntityRendererProvider.Context context) {
        super(context, new OgreModel());
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                       OgreEntity animatable, software.bernie.geckolib.cache.object.BakedGeoModel model,
                                       boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
    }
}