package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class FeatherProjectileRenderer extends ArrowRenderer<FeatherProjectileEntity> {

    public static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/feather_projectile.png");

    public FeatherProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FeatherProjectileEntity entity) {
        return TEXTURE;
    }
}
