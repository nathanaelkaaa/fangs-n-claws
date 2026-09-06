package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class TamedEvents {

    private TamedEvents() { }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (TamedRules.isFriendlyFire(source.getEntity(), event.getEntity())
                || TamedRules.isFriendlyFire(source.getDirectEntity(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        Entity mob = event.getEntity();
        LivingEntity target = event.getNewAboutToBeSetTarget();

        if (TamedRules.isFriendlyFire(mob, target) || TamedRules.isGuardianTruce(mob, target)) {
            event.setCanceled(true);
        }
    }
}
