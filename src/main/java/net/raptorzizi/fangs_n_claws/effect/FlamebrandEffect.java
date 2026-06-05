package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

public class FlamebrandEffect extends MobEffect {

    public static final int STACKS_REQUIRED            = 3;
    public static final int STACKS_REQUIRED_AMPLIFIER  = STACKS_REQUIRED - 1;

    public FlamebrandEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    protected RegistryObject<? extends MobEffect> getSelfEffect() {
        return MobEffectsRegistry.FLAMEBRAND;
    }

    public static void addFlamebrandStack(LivingEntity entity) {
        MobEffectInstance previous = entity.getEffect(MobEffectsRegistry.FLAMEBRAND.get());
        int newAmplifier = previous != null ? previous.getAmplifier() + 1 : 0;
        entity.addEffect(new MobEffectInstance(
                MobEffectsRegistry.FLAMEBRAND.get(), 20 * 10, newAmplifier,
                false, false, true));
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        ServerLevel level = (ServerLevel) entity.level();

        if (amplifier >= STACKS_REQUIRED_AMPLIFIER) {
            triggerExplosion(entity, level);
            entity.removeEffect(getSelfEffect().get());
            return;
        }

        spawnAmbientParticles(entity, level, amplifier);
    }

    private void spawnAmbientParticles(LivingEntity entity, ServerLevel level, int amplifier) {
        int count    = (amplifier + 1) * 6;
        var particle = amplifier >= 1 ? ParticlesRegistry.ORANGE_SMOKE.get() : ParticleTypes.SMOKE;
        level.sendParticles(particle,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                count, 0.3, 0.4, 0.3, 0.01);
    }

    protected void triggerExplosion(LivingEntity entity, ServerLevel level) {
        float radius    = 3.5f;
        float radiusSqr = radius * radius;
        float baseDamage = 8.0f;

        AABB box = entity.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.distanceToSqr(entity.position()) >= radiusSqr) continue;
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().magic(), baseDamage);
            target.setRemainingFireTicks(100);
        }

        var center = entity.getBoundingBox().getCenter();
        level.sendParticles(ParticlesRegistry.FIRE_EXPLOSION.get(),
                center.x, center.y, center.z, 20, 1.0, 1.0, 1.0, 0.05);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                2.5f, 0.9f + level.random.nextFloat() * 0.2f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        if (amplifier >= STACKS_REQUIRED_AMPLIFIER) return true;
        return duration % 8 == 0;
    }
}
