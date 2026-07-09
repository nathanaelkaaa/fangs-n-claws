package net.raptorzizi.fangs_n_claws.entity.owlbear;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BabyOwlbearRenderer extends GeoEntityRenderer<BabyOwlbearEntity> {

    private static final float SCALE = 2.0F;

    public BabyOwlbearRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyOwlbearModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    BabyOwlbearEntity animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale * SCALE, heightScale * SCALE, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
    }
}
