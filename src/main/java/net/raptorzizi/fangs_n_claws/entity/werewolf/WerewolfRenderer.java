package net.raptorzizi.fangs_n_claws.entity.werewolf;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WerewolfRenderer extends GeoEntityRenderer<WerewolfEntity> {

    public WerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfModel());
    }
}
