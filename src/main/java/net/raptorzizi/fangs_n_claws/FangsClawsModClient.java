package net.raptorzizi.fangs_n_claws;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;


@Mod(value = FangsClawsMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class FangsClawsModClient {
    public FangsClawsModClient(ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.OGRE.get(), OgreRenderer::new);
    }
}