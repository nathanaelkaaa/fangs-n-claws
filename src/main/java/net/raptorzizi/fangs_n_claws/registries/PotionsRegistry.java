package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class PotionsRegistry {

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<Potion, Potion> BLINDNESS =
            POTIONS.register("blindness",
                    () -> new Potion(new MobEffectInstance(MobEffects.BLINDNESS, 3600, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_BLINDNESS =
            POTIONS.register("long_blindness",
                    () -> new Potion("blindness",
                            new MobEffectInstance(MobEffects.BLINDNESS, 9600, 0)));

    public static final DeferredHolder<Potion, Potion> NAUSEA =
            POTIONS.register("nausea",
                    () -> new Potion(new MobEffectInstance(MobEffects.CONFUSION, 600, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
