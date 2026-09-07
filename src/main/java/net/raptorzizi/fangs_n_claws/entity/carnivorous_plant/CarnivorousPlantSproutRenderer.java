package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CarnivorousPlantSproutRenderer extends GeoEntityRenderer<CarnivorousPlantSproutEntity> {

    public CarnivorousPlantSproutRenderer(EntityRendererProvider.Context context) {
        super(context, new CarnivorousPlantSproutModel());
        this.shadowRadius = 0.3F;
    }
}
