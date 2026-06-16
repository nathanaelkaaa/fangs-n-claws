package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.function.Supplier;

public class SoundsRegistry {

    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, FangsClawsMod.MOD_ID);

    public static final Supplier<SoundEvent> HEAVY_IMPACT =
            register("generic.heavy_impact");
    public static final Supplier<SoundEvent> ROCK_IMPACT =
            register("generic.rock_impact");
    public static final Supplier<SoundEvent> BACKSTAB_IMPACT =
            register("generic.backstab_impact");
    public static final Supplier<SoundEvent> OGRE_AMBIENT =
            register("ogre.ambient");
    public static final Supplier<SoundEvent> OGRE_HURT =
            register("ogre.hurt");
    public static final Supplier<SoundEvent> OGRE_DEATH =
            register("ogre.death");
    public static final Supplier<SoundEvent> OGRE_ROAR =
            register("ogre.roar");
    public static final Supplier<SoundEvent> HELL_OGRE_FIRE_BREATH =
            register("hell_ogre.fire_breath");

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

    public static final Supplier<SoundEvent> BLOWGUN_SHOOT =
            register("blowgun.shoot");
    public static final Supplier<SoundEvent> DART_HIT =
            register("dart.hit");

    public static final Supplier<SoundEvent> SCORPION_DEATH =
            register("scorpion.death");
    public static final Supplier<SoundEvent> SCORPION_HURT =
            register("scorpion.hurt");
    public static final Supplier<SoundEvent> SCORPION_STINGER =
            register("scorpion.stinger");
    public static final Supplier<SoundEvent> SCORPION_CLAW1 =
            register("scorpion.claw1");
    public static final Supplier<SoundEvent> SCORPION_CLAW2 =
            register("scorpion.claw2");

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
