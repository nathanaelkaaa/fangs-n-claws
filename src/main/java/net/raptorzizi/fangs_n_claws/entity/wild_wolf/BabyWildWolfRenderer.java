package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BabyWildWolfRenderer extends GeoEntityRenderer<BabyWildWolfEntity> {

    public BabyWildWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyWildWolfModel());
        this.shadowRadius = 0.3F;
    }
}
