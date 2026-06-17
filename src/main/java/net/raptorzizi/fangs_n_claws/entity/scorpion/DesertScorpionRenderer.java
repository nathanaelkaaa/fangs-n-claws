package net.raptorzizi.fangs_n_claws.entity.scorpion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DesertScorpionRenderer extends GeoEntityRenderer<DesertScorpionEntity> {

    private static final float SCALE = 1.3F;

    public DesertScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new DesertScorpionModel());
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    DesertScorpionEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack,
                animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
