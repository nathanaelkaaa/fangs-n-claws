package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffectInstance;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HellFlamebrandEffect extends FlamebrandEffect {

    public HellFlamebrandEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }

    public static void addHellFlamebrandStack(LivingEntity entity) {
        addHellFlamebrandStack(entity, null);
    }

    public static void addHellFlamebrandStack(LivingEntity entity, @Nullable LivingEntity source) {
        rememberSource(entity, source);
        MobEffectInstance previous = entity.getEffect(MobEffectsRegistry.HELLFIRE_FLAMEBRAND);
        int newAmplifier = previous != null ? previous.getAmplifier() + 1 : 0;
        entity.addEffect(new MobEffectInstance(
                MobEffectsRegistry.HELLFIRE_FLAMEBRAND, 20 * 10, newAmplifier,
                false, false, true));
    }

    @Override
    protected Holder<MobEffect> getSelfHolder() {
        return MobEffectsRegistry.HELLFIRE_FLAMEBRAND;
    }

    @Override
    protected void triggerExplosion(LivingEntity entity, ServerLevel level) {
        float radius = 5.0f;
        float radiusSqr = radius * radius;
        float baseDamage = 12.0f;

        UUID sourceId = consumeSource(entity);

        AABB box = entity.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.distanceToSqr(entity.position()) >= radiusSqr) continue;
            if (sourceId != null && target.getUUID().equals(sourceId)) continue; // pas de degat a la source
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().magic(), baseDamage);
            target.setRemainingFireTicks(100);
        }

        var center = entity.getBoundingBox().getCenter();
        level.sendParticles(ParticlesRegistry.FIRE_EXPLOSION.get(),
                center.x, center.y, center.z, 35, 1.0, 1.0, 1.0, 0.06);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
                2.5f, 0.9f + level.random.nextFloat() * 0.2f);
    }
}
