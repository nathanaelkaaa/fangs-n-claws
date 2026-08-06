package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class AcidEffect extends MobEffect {

    private static final double ARMOR_FACTOR = -0.5;
    public static final float DURABILITY_MULTIPLIER = 2.0f;

    public AcidEffect() {
        super(MobEffectCategory.HARMFUL, 0x76F154);
        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "acid_armor"),
                ARMOR_FACTOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "acid_armor_toughness"),
                ARMOR_FACTOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
