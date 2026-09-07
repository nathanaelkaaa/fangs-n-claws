package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class TamedEvents {

    private TamedEvents() { }

    @SubscribeEvent
    public static void onIncomingDamage(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (TamedRules.isFriendlyFire(source.getEntity(), event.getEntity())
                || TamedRules.isFriendlyFire(source.getDirectEntity(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        Entity mob = event.getEntity();
        LivingEntity target = event.getNewTarget();

        if (TamedRules.isFriendlyFire(mob, target) || TamedRules.isGuardianTruce(mob, target)) {
            event.setCanceled(true);
        }
    }
}
