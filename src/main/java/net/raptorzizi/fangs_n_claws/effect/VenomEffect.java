package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class VenomEffect extends MobEffect {

    private static final UUID VENOM_SLOWNESS_UUID =
            UUID.nameUUIDFromBytes("fangs_n_claws:venom_slowness".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public VenomEffect() {
        super(MobEffectCategory.HARMFUL, 0x9400D3);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                VENOM_SLOWNESS_UUID.toString(),
                -0.15,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Behaviour handled in FangsClawsMod.onLivingTick
    }
}
