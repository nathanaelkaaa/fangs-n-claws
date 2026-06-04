package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.CatchingClawItem;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteClawItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteFangDaggerItem;

public class EnchantmentsRegistry {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, FangsClawsMod.MOD_ID);

    /** Fang Dagger / Netherite Dagger uniquement : +50 % de dégâts de backstab */
    public static final RegistryObject<Enchantment> CRITICAL_BACKSTAB = ENCHANTMENTS.register("critical_backstab",
            () -> new Enchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 1; }
                @Override public int getMinCost(int lvl) { return 15; }
                @Override public int getMaxCost(int lvl) { return 50; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof FangDaggerItem
                        || stack.getItem() instanceof NetheriteFangDaggerItem;
                }
            });

    /** Fang Dagger / Netherite Dagger uniquement : réduit le cooldown du backstab */
    public static final RegistryObject<Enchantment> QUICK_KILLER = ENCHANTMENTS.register("quick_killer",
            () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 2; }
                @Override public int getMinCost(int lvl) { return 5 + (lvl - 1) * 8; }
                @Override public int getMaxCost(int lvl) { return getMinCost(lvl) + 20; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof FangDaggerItem
                        || stack.getItem() instanceof NetheriteFangDaggerItem;
                }
            });

    /** Catching Claw / Netherite Claw uniquement : permet d'accrocher les ItemEntity */
    public static final RegistryObject<Enchantment> ITEM_CATCHER = ENCHANTMENTS.register("item_catcher",
            () -> new Enchantment(Enchantment.Rarity.RARE, EnchantmentCategory.BREAKABLE,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 1; }
                @Override public int getMinCost(int lvl) { return 15; }
                @Override public int getMaxCost(int lvl) { return 50; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof CatchingClawItem
                        || stack.getItem() instanceof NetheriteClawItem;
                }
            });

    /** Catching Claw / Netherite Claw uniquement : +0.25 de dégâts d'attaque par niveau */
    public static final RegistryObject<Enchantment> SCRATCH = ENCHANTMENTS.register("scratch",
            () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 5; }
                @Override public int getMinCost(int lvl) { return 5 + (lvl - 1) * 8; }
                @Override public int getMaxCost(int lvl) { return getMinCost(lvl) + 20; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof CatchingClawItem
                        || stack.getItem() instanceof NetheriteClawItem;
                }
            });

    // Helper

    public static int getLevel(ItemStack stack, RegistryObject<Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment.get(), stack);
    }

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }
}
