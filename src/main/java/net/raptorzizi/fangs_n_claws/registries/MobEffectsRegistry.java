package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.effect.StunnedEffect;
import net.raptorzizi.fangs_n_claws.effect.VenomEffect;

public class MobEffectsRegistry {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<MobEffect, BleedingEffect> BLEEDING =
            MOB_EFFECTS.register("bleeding", BleedingEffect::new);

    public static final DeferredHolder<MobEffect, StunnedEffect> STUNNED =
            MOB_EFFECTS.register("stunned", StunnedEffect::new);

    public static final DeferredHolder<MobEffect, VenomEffect> VENOM =
            MOB_EFFECTS.register("venom", VenomEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
