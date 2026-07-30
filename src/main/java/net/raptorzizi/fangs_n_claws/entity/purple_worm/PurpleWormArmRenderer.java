package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurpleWormArmRenderer extends GeoEntityRenderer<PurpleWormArmEntity> {

    public PurpleWormArmRenderer(EntityRendererProvider.Context context) {
        super(context, new PurpleWormArmModel());
        this.shadowRadius = 0.6f;
    }
}
