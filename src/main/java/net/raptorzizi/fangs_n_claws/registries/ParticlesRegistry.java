package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class ParticlesRegistry {

    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_PARTICLE =
            PARTICLES.register("blood", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_GROUND =
            PARTICLES.register("blood_ground", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STUN_STAR =
            PARTICLES.register("stun_star", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLACK_FOG =
            PARTICLES.register("black_fog", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_EXPLOSION =
            PARTICLES.register("fire_explosion", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE =
            PARTICLES.register("fire", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ORANGE_SMOKE =
            PARTICLES.register("orange_smoke", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_SPLASH =
            PARTICLES.register("acid_splash", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_CLOUD =
            PARTICLES.register("acid_cloud", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_BUBBLE =
            PARTICLES.register("acid_bubble", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_TRAIL =
            PARTICLES.register("acid_trail", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
