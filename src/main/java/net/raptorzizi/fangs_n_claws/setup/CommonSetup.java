package net.raptorzizi.fangs_n_claws.setup;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
// bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.OGRE.get(),     OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREWOLF.get(), WerewolfEntity.prepareAttributes().build());
    }
}