package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.function.Supplier;

public class SoundsRegistry {

    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, FangsClawsMod.MOD_ID);
    //TODO Create ogre sounds (actual are from mo creatures)
    public static final Supplier<SoundEvent> OGRE_AMBIENT =
            register("ogre.ambient");
    public static final Supplier<SoundEvent> OGRE_HURT =
            register("ogre.hurt");
    public static final Supplier<SoundEvent> OGRE_DEATH =
            register("ogre.death");

    private static Supplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FangsClawsMod.id(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
