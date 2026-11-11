package net.raptorzizi.fangs_n_claws.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.armor.ScorpionArmorItem;

public final class AcidImmunity {

    public static final TagKey<EntityType<?>> ACID_IMMUNE =
            TagKey.create(Registries.ENTITY_TYPE, FangsClawsMod.id("acid_immune"));

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private AcidImmunity() {}

    public static boolean isImmune(Entity entity) {
        return entity.getType().is(ACID_IMMUNE) || wearsFullChitin(entity);
    }

    public static boolean wearsFullChitin(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(living.getItemBySlot(slot).getItem() instanceof ScorpionArmorItem)) return false;
        }
        return true;
    }
}
