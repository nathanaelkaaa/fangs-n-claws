package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FrozenEffect extends MobEffect {

    private static final UUID FROZEN_SPEED_UUID =
            UUID.nameUUIDFromBytes("fangs_n_claws:frozen_speed".getBytes(StandardCharsets.UTF_8));

    public FrozenEffect() {
        super(MobEffectCategory.HARMFUL, 0xA0CFFF);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                FROZEN_SPEED_UUID.toString(),
                -1.0,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(0, Math.min(entity.getDeltaMovement().y, 0), 0);

        if (entity.tickCount % 4 == 0 && entity.level() instanceof ServerLevel serverLevel) {
            double cx = entity.getX();
            double cy = entity.getY() + entity.getBbHeight() * 0.5;
            double cz = entity.getZ();
            double rx = entity.getBbWidth() * 0.6;
            double ry = entity.getBbHeight() * 0.5;
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy, cz, 3, rx, ry, rx, 0.03);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0;
    }
}
