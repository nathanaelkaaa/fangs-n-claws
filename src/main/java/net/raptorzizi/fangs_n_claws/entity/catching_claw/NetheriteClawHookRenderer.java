package net.raptorzizi.fangs_n_claws.entity.catching_claw;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class NetheriteClawHookRenderer extends CatchingClawHookRenderer<NetheriteClawHookEntity> {

    public NetheriteClawHookRenderer(EntityRendererProvider.Context context) {
        super(context, FangsClawsMod.id("textures/entity/catching_claw_hook_netherite.png"));
    }
}
