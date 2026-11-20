package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EntRenderer extends GeoEntityRenderer<EntEntity> {

    private static final float SHADOW_RADIUS = 0.9F;

    public EntRenderer(EntityRendererProvider.Context context) {
        super(context, new EntModel());
        this.addRenderLayer(new EntLeavesLayer(this));
        this.shadowRadius = SHADOW_RADIUS;
    }
}
