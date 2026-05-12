package net.raptorzizi.fangs_n_claws.setup;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

@EventBusSubscriber(
        modid = FangsClawsMod.MOD_ID,
        //bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.OGRE.get(), OgreRenderer::new);
    }
}