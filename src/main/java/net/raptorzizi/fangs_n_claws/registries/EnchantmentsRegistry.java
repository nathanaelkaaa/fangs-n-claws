package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class EnchantmentsRegistry {

    public static final ResourceKey<Enchantment> CRITICAL_BACKSTAB = key("critical_backstab");
    public static final ResourceKey<Enchantment> QUICK_KILLER = key("quick_killer");
    public static final ResourceKey<Enchantment> ITEM_CATCHER = key("item_catcher");
    public static final ResourceKey<Enchantment> SCRATCH = key("scratch");
    public static final ResourceKey<Enchantment> BLAZING = key("blazing");

    public static int getLevel(ItemStack stack, Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(h -> stack.getEnchantmentLevel(h))
                .orElse(0);
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, name));
    }
}
