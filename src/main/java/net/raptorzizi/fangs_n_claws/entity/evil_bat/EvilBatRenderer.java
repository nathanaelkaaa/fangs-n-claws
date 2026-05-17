package net.raptorzizi.fangs_n_claws.entity.evil_bat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EvilBatRenderer extends GeoEntityRenderer<EvilBatEntity> {

    // Spawn

    public EvilBatRenderer(EntityRendererProvider.Context context) {
        super(context, new EvilBatModel());
        this.shadowRadius = 0.2f;
    }

    // Animation

    @Override
    protected float getDeathMaxRotation(EvilBatEntity animatable) {
        return 0f;
    }
}
