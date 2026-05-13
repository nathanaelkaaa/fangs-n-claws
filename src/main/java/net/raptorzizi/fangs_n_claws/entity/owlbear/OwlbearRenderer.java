package net.raptorzizi.fangs_n_claws.entity.owlbear;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OwlbearRenderer extends GeoEntityRenderer<OwlbearEntity> {

    private static final float SCALE = 2.0F;

    public OwlbearRenderer(EntityRendererProvider.Context context) {
        super(context, new OwlbearModel());
        this.shadowRadius = 0.8f;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    OwlbearEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
    }
}
