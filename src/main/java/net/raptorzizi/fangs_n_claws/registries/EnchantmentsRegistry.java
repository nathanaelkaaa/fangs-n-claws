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
import net.raptorzizi.fangs_n_claws.item.FirePitchforkItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteClawItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteFangDaggerItem;

public class EnchantmentsRegistry {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, FangsClawsMod.MOD_ID);

    public static final RegistryObject<Enchantment> CRITICAL_BACKSTAB = ENCHANTMENTS.register("critical_backstab",
            () -> new Enchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 3; }
                @Override public int getMinCost(int lvl) { return 1 + (lvl - 1) * 10; }
                @Override public int getMaxCost(int lvl) { return 55 + (lvl - 1) * 10; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof FangDaggerItem
                        || stack.getItem() instanceof NetheriteFangDaggerItem;
                }
                // canEnchant() alone only gates /enchant, the anvil and loot-table enchanting;
                // the enchanting table itself calls canApplyAtEnchantingTable() directly and
                // otherwise falls back to the (much broader) EnchantmentCategory predicate.
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
                    return canEnchant(stack);
                }
                @Override public boolean checkCompatibility(Enchantment other) {
                    return super.checkCompatibility(other) && other != QUICK_KILLER.get();
                }
            });

    public static final RegistryObject<Enchantment> QUICK_KILLER = ENCHANTMENTS.register("quick_killer",
            () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 2; }
                @Override public int getMinCost(int lvl) { return 5 + (lvl - 1) * 8; }
                @Override public int getMaxCost(int lvl) { return 25 + (lvl - 1) * 8; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof FangDaggerItem
                        || stack.getItem() instanceof NetheriteFangDaggerItem;
                }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
                    return canEnchant(stack);
                }
                @Override public boolean checkCompatibility(Enchantment other) {
                    return super.checkCompatibility(other) && other != CRITICAL_BACKSTAB.get();
                }
            });

    public static final RegistryObject<Enchantment> ITEM_CATCHER = ENCHANTMENTS.register("item_catcher",
            () -> new Enchantment(Enchantment.Rarity.RARE, EnchantmentCategory.BREAKABLE,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 1; }
                @Override public int getMinCost(int lvl) { return 10; }
                @Override public int getMaxCost(int lvl) { return 40; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof CatchingClawItem
                        || stack.getItem() instanceof NetheriteClawItem;
                }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
                    return canEnchant(stack);
                }
            });

    public static final RegistryObject<Enchantment> SCRATCH = ENCHANTMENTS.register("scratch",
            () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 5; }
                @Override public int getMinCost(int lvl) { return 1 + (lvl - 1) * 8; }
                @Override public int getMaxCost(int lvl) { return 25 + (lvl - 1) * 8; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof CatchingClawItem
                        || stack.getItem() instanceof NetheriteClawItem;
                }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
                    return canEnchant(stack);
                }
            });

    public static final RegistryObject<Enchantment> BLAZING = ENCHANTMENTS.register("blazing",
            () -> new Enchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,
                    new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public int getMaxLevel()       { return 5; }
                @Override public int getMinCost(int lvl) { return 1 + (lvl - 1) * 8; }
                @Override public int getMaxCost(int lvl) { return 25 + (lvl - 1) * 8; }
                @Override public boolean canEnchant(ItemStack stack) {
                    return stack.getItem() instanceof FirePitchforkItem;
                }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) {
                    return canEnchant(stack);
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
