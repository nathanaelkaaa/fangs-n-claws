package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AcidEffect extends MobEffect {

    private static final double ARMOR_FACTOR = -0.5;
    public static final float DURABILITY_MULTIPLIER = 2.0f;

    // 1.20.1 : les modificateurs d'effet sont identifies par une UUID (String), pas un ResourceLocation.
    private static final String ARMOR_UUID           = "b7e5c3a2-4f1d-4c8e-9a6b-1f2e3d4c5b6a";
    private static final String ARMOR_TOUGHNESS_UUID = "c8f6d4b3-5e2c-4d9f-8b7a-2e3f4d5c6b7a";

    public AcidEffect() {
        super(MobEffectCategory.HARMFUL, 0x76F154);
        // ADD_MULTIPLIED_TOTAL (1.21) == MULTIPLY_TOTAL (1.20.1)
        this.addAttributeModifier(Attributes.ARMOR,
                ARMOR_UUID, ARMOR_FACTOR, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                ARMOR_TOUGHNESS_UUID, ARMOR_FACTOR, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
