package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

public class StunnedEffect extends MobEffect {

    // ---- Inspired by Alex's Caves https://github.com/AlexModGuy/AlexsCaves/blob/main/src/main/java/com/github/alexmodguy/alexscaves/server/potion/StunnedEffect.java ----

    public StunnedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFBC5);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "stunned_speed"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getDeltaMovement().y > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0.1, 1));
        }

        if (entity.level().isClientSide && entity.level().random.nextFloat() < entity.getBbWidth() * 0.12F) {
            entity.level().addParticle(
                    ParticlesRegistry.STUN_STAR.get(),
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    entity.getId(),
                    entity.level().random.nextFloat() * 360f,
                    0
            );
        }

        if (entity instanceof Mob mob && !mob.level().isClientSide) {
            entity.setXRot(30.0F);
            entity.xRotO = 30.0F;
            mob.goalSelector.setControlFlag(Goal.Flag.MOVE, false);
            mob.goalSelector.setControlFlag(Goal.Flag.JUMP, false);
            mob.goalSelector.setControlFlag(Goal.Flag.LOOK, false);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }
}
