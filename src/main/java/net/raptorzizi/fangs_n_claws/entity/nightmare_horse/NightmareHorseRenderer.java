package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseArmorLayer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareHorseRenderer extends GeoEntityRenderer<NightmareHorseEntity> {

    public NightmareHorseRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareHorseModel());
        this.shadowRadius = 0.7F;

        this.addRenderLayer(new HorseArmorLayer<>(this));
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    NightmareHorseEntity animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack,
                animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
