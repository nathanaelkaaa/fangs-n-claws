package net.raptorzizi.fangs_n_claws.setup;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
// bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.OGRE.get(),           OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREWOLF.get(),       WerewolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.OWLBEAR.get(),        OwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.SILVER_SKELETON.get(), SilverSkeletonEntity.prepareAttributes().build());
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity.hasEffect(MobEffectsRegistry.STUNNED)) {
            entity.removeEffect(MobEffectsRegistry.STUNNED);
        }
    }
}