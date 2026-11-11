package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurpleGrubRenderer extends GeoEntityRenderer<PurpleGrubEntity> {

    public PurpleGrubRenderer(EntityRendererProvider.Context context) {
        super(context, new PurpleGrubModel());
        this.shadowRadius = 0.4F;
    }
}
