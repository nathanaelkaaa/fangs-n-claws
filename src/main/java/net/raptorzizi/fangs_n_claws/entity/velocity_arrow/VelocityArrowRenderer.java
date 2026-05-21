package net.raptorzizi.fangs_n_claws.entity.velocity_arrow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class VelocityArrowRenderer extends ArrowRenderer<VelocityArrowEntity> {

    public static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/velocity_arrow.png");

    public VelocityArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(VelocityArrowEntity entity) {
        return TEXTURE;
    }
}
