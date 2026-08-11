package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.minecraft.world.effect.MobEffectCategory;
import net.raptorzizi.fangs_n_claws.effect.AcidEffect;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.effect.FrozenEffect;
import net.raptorzizi.fangs_n_claws.effect.HellFlamebrandEffect;
import net.raptorzizi.fangs_n_claws.effect.MithridaticEffect;
import net.raptorzizi.fangs_n_claws.effect.StunnedEffect;
import net.raptorzizi.fangs_n_claws.effect.VenomEffect;

public class MobEffectsRegistry {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, FangsClawsMod.MOD_ID);

    public static final RegistryObject<BleedingEffect> BLEEDING =
            MOB_EFFECTS.register("bleeding", BleedingEffect::new);

    public static final RegistryObject<StunnedEffect> STUNNED =
            MOB_EFFECTS.register("stunned", StunnedEffect::new);

    public static final RegistryObject<VenomEffect> VENOM =
            MOB_EFFECTS.register("venom", VenomEffect::new);

    public static final RegistryObject<FlamebrandEffect> FLAMEBRAND =
            MOB_EFFECTS.register("flamebrand", () -> new FlamebrandEffect(MobEffectCategory.HARMFUL, 0xFF4500));

    public static final RegistryObject<HellFlamebrandEffect> HELLFIRE_FLAMEBRAND =
            MOB_EFFECTS.register("hellfire_flamebrand", HellFlamebrandEffect::new);

    public static final RegistryObject<MithridaticEffect> MITHRIDATIC =
            MOB_EFFECTS.register("mithridatic", MithridaticEffect::new);

    public static final RegistryObject<FrozenEffect> FROZEN =
            MOB_EFFECTS.register("frozen", FrozenEffect::new);

    public static final RegistryObject<AcidEffect> ACID =
            MOB_EFFECTS.register("acid", AcidEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
