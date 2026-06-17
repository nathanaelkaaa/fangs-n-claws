package net.raptorzizi.fangs_n_claws.entity.scorpion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrostScorpionRenderer extends GeoEntityRenderer<FrostScorpionEntity> {

    private static final float SCALE = 1.3F;

    public FrostScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new FrostScorpionModel());
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    FrostScorpionEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack,
                animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
