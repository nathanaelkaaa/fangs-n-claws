package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class ParticlesRegistry {

    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, FangsClawsMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> BLOOD_PARTICLE =
            PARTICLES.register("blood", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BLOOD_GROUND =
            PARTICLES.register("blood_ground", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> STUN_STAR =
            PARTICLES.register("stun_star", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BLACK_FOG =
            PARTICLES.register("black_fog", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_EXPLOSION =
            PARTICLES.register("fire_explosion", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> ORANGE_SMOKE =
            PARTICLES.register("orange_smoke", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
