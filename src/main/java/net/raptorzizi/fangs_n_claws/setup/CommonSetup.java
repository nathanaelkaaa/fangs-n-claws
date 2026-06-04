package net.raptorzizi.fangs_n_claws.setup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

// Mod bus events (attribute registration)
@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID, bus = Bus.MOD)
public class CommonSetup {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.EVIL_BAT.get(),         EvilBatEntity.prepareAttributes().build());
        event.put(EntityRegistry.OGRE.get(),             OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.CAVE_OGRE.get(),        OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREWOLF.get(),         WerewolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.OWLBEAR.get(),          OwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.SILVER_SKELETON.get(),  SilverSkeletonEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOLEM.get(),            GolemEntity.prepareAttributes().build());
        event.put(EntityRegistry.GHOST.get(),            GhostEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOBLIN.get(),           GoblinEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREVILLAGER.get(),     WerevillagerEntity.prepareAttributes().build());
    }

    // Forge bus events (game events)
    @Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID, bus = Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide() && entity.hasEffect(MobEffectsRegistry.STUNNED.get())) {
                entity.removeEffect(MobEffectsRegistry.STUNNED.get());
            }
        }
    }
}
