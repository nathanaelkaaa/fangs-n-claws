package net.raptorzizi.fangs_n_claws.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

@JeiPlugin
public class FangsClawsJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = FangsClawsMod.id("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        info(reg, "owlbear",         ItemsRegistry.OWLBEAR_SPAWN_EGG.get());
        info(reg, "shrike",          ItemsRegistry.SHRIKE_SPAWN_EGG.get());
        info(reg, "scorpion",        ItemsRegistry.SCORPION_SPAWN_EGG.get());
        info(reg, "desert_scorpion", ItemsRegistry.DESERT_SCORPION_SPAWN_EGG.get());
        info(reg, "frost_scorpion",  ItemsRegistry.FROST_SCORPION_SPAWN_EGG.get());
        info(reg, "nether_scorpion", ItemsRegistry.NETHER_SCORPION_SPAWN_EGG.get());

        info(reg, "giant_feather",   ItemsRegistry.GIANT_FEATHER.get());
        info(reg, "snow_duvet",      ItemsRegistry.SNOW_DUVET.get());
        info(reg, "chitin",          ItemsRegistry.CHITIN.get());
        info(reg, "fur",             ItemsRegistry.FUR.get());
        info(reg, "heavy_claw",      ItemsRegistry.HEAVY_CLAW.get());
        info(reg, "long_fang",       ItemsRegistry.LONG_FANG.get());
        info(reg, "scorpion_sting",  ItemsRegistry.SCORPION_STING.get());
        info(reg, "black_horn",      ItemsRegistry.BLACK_HORN.get());
        info(reg, "spectral_essence", ItemsRegistry.SPECTRAL_ESSENCE.get());
        info(reg, "vile_fat",        ItemsRegistry.VILE_FAT.get());

        info(reg, "blowgun",          ItemsRegistry.BLOWGUN.get());
        info(reg, "poisonous_dart",   ItemsRegistry.POISONOUS_DART.get());
        info(reg, "fang_dagger",      ItemsRegistry.FANG_DAGGER.get());
        info(reg, "netherite_dagger", ItemsRegistry.NETHERITE_DAGGER.get());
        info(reg, "catching_claw",    ItemsRegistry.CATCHING_CLAW.get());
        info(reg, "catching_claw_netherite", ItemsRegistry.CATCHING_CLAW_NETHERITE.get());
        info(reg, "silver_sword",     ItemsRegistry.SILVER_SWORD.get());
        info(reg, "tomahawk",         ItemsRegistry.TOMAHAWK.get());
        info(reg, "fire_pitchfork",   ItemsRegistry.FIRE_PITCHFORK.get());
        info(reg, "hellfire_pitchfork", ItemsRegistry.HELLFIRE_PITCHFORK.get());
        info(reg, "evil_eye",         ItemsRegistry.EVIL_EYE.get());
        info(reg, "velocity_arrow",   ItemsRegistry.VELOCITY_ARROW.get());

        info(reg, "ghost_block",  ItemsRegistry.GHOST_BLOCK.get());
        info(reg, "vile_lantern", ItemsRegistry.VILE_LANTERN.get());
        info(reg, "bear_trap",    ItemsRegistry.BEAR_TRAP.get());

        info(reg, "sturdy_saddle",  ItemsRegistry.STURDY_SADDLE.get());
        info(reg, "horse_blanket",  ItemsRegistry.HORSE_BLANKET.get());
        info(reg, "totem_of_frost", ItemsRegistry.TOTEM_OF_FROST.get());

        info(reg, "shrike_upgrade_template", ItemsRegistry.SHRIKE_UPGRADE_SMITHING_TEMPLATE.get());
        info(reg, "scorpion_armor",
                ItemsRegistry.SCORPION_HELMET.get(), ItemsRegistry.SCORPION_CHESTPLATE.get(),
                ItemsRegistry.SCORPION_LEGGINGS.get(), ItemsRegistry.SCORPION_BOOTS.get());
        info(reg, "fur_armor",
                ItemsRegistry.FUR_HELMET.get(), ItemsRegistry.FUR_CHESTPLATE.get(),
                ItemsRegistry.FUR_LEGGINGS.get(), ItemsRegistry.FUR_BOOTS.get());
        info(reg, "owl_armor",
                ItemsRegistry.OWL_HELMET.get(), ItemsRegistry.OWL_CHESTPLATE.get(),
                ItemsRegistry.OWL_LEGGINGS.get(), ItemsRegistry.OWL_BOOTS.get());
        info(reg, "shrike_armor",
                ItemsRegistry.SHRIKE_HELMET.get(), ItemsRegistry.SHRIKE_CHESTPLATE.get(),
                ItemsRegistry.SHRIKE_LEGGINGS.get(), ItemsRegistry.SHRIKE_BOOTS.get());
    }

    private static void info(IRecipeRegistration reg, String key, ItemLike... items) {
        Component desc = Component.translatable("jei.fangs_n_claws." + key);
        for (ItemLike item : items) {
            reg.addIngredientInfo(item, desc);
        }
    }
}
