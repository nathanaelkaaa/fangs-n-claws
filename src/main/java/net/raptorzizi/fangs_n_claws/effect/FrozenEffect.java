package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class FrozenEffect extends MobEffect {

    public FrozenEffect() {
        super(MobEffectCategory.HARMFUL, 0xA0CFFF);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "frozen_speed"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(0, Math.min(entity.getDeltaMovement().y, 0), 0);

        if (entity.tickCount % 4 == 0 && entity.level() instanceof ServerLevel serverLevel) {
            double cx = entity.getX();
            double cy = entity.getY() + entity.getBbHeight() * 0.5;
            double cz = entity.getZ();
            double rx = entity.getBbWidth() * 0.6;
            double ry = entity.getBbHeight() * 0.5;
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy, cz, 3, rx, ry, rx, 0.03);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }
}
