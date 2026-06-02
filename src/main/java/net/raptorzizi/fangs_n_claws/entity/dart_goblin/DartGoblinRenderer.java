package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DartGoblinRenderer extends GeoEntityRenderer<DartGoblinEntity> {

    public DartGoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new DartGoblinModel());
        this.shadowRadius = 0.3f;
    }
}
