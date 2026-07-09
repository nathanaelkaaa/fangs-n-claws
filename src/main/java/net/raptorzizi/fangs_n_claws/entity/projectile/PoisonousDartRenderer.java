package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class PoisonousDartRenderer extends ArrowRenderer<PoisonousDartEntity> {

    private static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/poisonous_dart.png");

    public PoisonousDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PoisonousDartEntity entity) {
        return TEXTURE;
    }
}
