package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CarnivorousPlantRenderer extends GeoEntityRenderer<CarnivorousPlantEntity> {

    public CarnivorousPlantRenderer(EntityRendererProvider.Context context) {
        super(context, new CarnivorousPlantModel());
        this.shadowRadius = 0.4F;
    }
}
