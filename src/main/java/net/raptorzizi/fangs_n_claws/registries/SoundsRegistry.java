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

    public static final Supplier<SoundEvent> HEAVY_IMPACT =
            register("generic.heavy_impact");
    public static final Supplier<SoundEvent> ROCK_IMPACT =
            register("generic.rock_impact");

    //TODO Create ogre sounds (actual are from mo creatures)
    public static final Supplier<SoundEvent> OGRE_AMBIENT =
            register("ogre.ambient");
    public static final Supplier<SoundEvent> OGRE_HURT =
            register("ogre.hurt");
    public static final Supplier<SoundEvent> OGRE_DEATH =
            register("ogre.death");

    public static final Supplier<SoundEvent> OWLBEAR_HOWL =
            register("owlbear.howl");
    public static final Supplier<SoundEvent> OWLBEAR_HURT =
            register("owlbear.hurt");
    public static final Supplier<SoundEvent> OWLBEAR_DEATH =
            register("owlbear.death");
    public static final Supplier<SoundEvent> OWLBEAR_FLAP =
            register("owlbear.flap");
    public static final Supplier<SoundEvent> OWLBEAR_SLASH =
            register("owlbear.slash");

    public static final Supplier<SoundEvent> GOBLIN_AMBIENT =
            register("goblin.ambient");
    public static final Supplier<SoundEvent> GOBLIN_HURT =
            register("goblin.hurt");
    public static final Supplier<SoundEvent> GOBLIN_DEATH =
            register("goblin.death");
    public static final Supplier<SoundEvent> GOBLIN_CELEBRATE =
            register("goblin.celebrate");

    public static final Supplier<SoundEvent> GOLEM_AMBIENT =
            register("golem.ambient");
    public static final Supplier<SoundEvent> GOLEM_HURT =
            register("golem.hurt");
    public static final Supplier<SoundEvent> GOLEM_DEATH =
            register("golem.death");

    //TODO Create werewolf sounds (actual are from mo creatures)
    public static final Supplier<SoundEvent> WEREWOLF_AMBIENT =
            register("werewolf.ambient");
    public static final Supplier<SoundEvent> WEREWOLF_HOWL =
            register("werewolf.howl");
    public static final Supplier<SoundEvent> WEREWOLF_BITE =
            register("werewolf.bite");
    public static final Supplier<SoundEvent> WEREWOLF_HURT =
            register("werewolf.hurt");
    public static final Supplier<SoundEvent> WEREWOLF_DEATH =
            register("werewolf.death");

    private static Supplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FangsClawsMod.id(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
