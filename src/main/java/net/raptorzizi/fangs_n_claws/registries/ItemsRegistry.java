package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.BlowgunItem;
import net.raptorzizi.fangs_n_claws.item.FirePitchforkItem;
import net.raptorzizi.fangs_n_claws.item.HellFirePitchforkItem;
import net.raptorzizi.fangs_n_claws.item.CatchingClawItem;
import net.raptorzizi.fangs_n_claws.item.EvilEyeItem;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteClawItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteFangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.SilverSwordItem;
import net.raptorzizi.fangs_n_claws.item.PoisonousDartItem;
import net.raptorzizi.fangs_n_claws.item.VelocityArrowItem;
import net.raptorzizi.fangs_n_claws.item.VileFatItem;
import net.minecraft.world.item.ArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.FurArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.OwlArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.ShrikeArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.ScorpionArmorItem;
import java.util.function.Supplier;

public class ItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FangsClawsMod.MOD_ID);

    /**
     * Spawn eggs
     */
    public static final Supplier<DeferredSpawnEggItem> OGRE_SPAWN_EGG =  ITEMS.register("ogre_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.OGRE, 0x418e4e, 0x14422e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> CAVE_OGRE_SPAWN_EGG = ITEMS.register("cave_ogre_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.CAVE_OGRE, 0x3a3230, 0x1a90c8, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> HELL_OGRE_SPAWN_EGG = ITEMS.register("hell_ogre_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.HELL_OGRE, 0x7a1e00, 0xff6600, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> WEREWOLF_SPAWN_EGG =  ITEMS.register("werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.WEREWOLF, 0x39363c, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> OWLBEAR_SPAWN_EGG =  ITEMS.register("owlbear_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.OWLBEAR, 0x554030, 0x2d2625, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> SHRIKE_SPAWN_EGG =  ITEMS.register("shrike_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.SHRIKE, 0x9fb0bd, 0xe0edf3, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> SILVER_SKELETON_SPAWN_EGG = ITEMS.register("silver_skeleton_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.SILVER_SKELETON, 0x7b7871, 0xc2c0be, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> GOLEM_SPAWN_EGG = ITEMS.register("golem_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.GOLEM, 0x9b775b, 0x52a12f, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> ICE_GOLEM_SPAWN_EGG = ITEMS.register("ice_golem_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.ICE_GOLEM, 0xa8d8e8, 0xffffff, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> EVIL_BAT_SPAWN_EGG = ITEMS.register("evil_bat_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.EVIL_BAT, 0x43372f, 0xe8d063, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> GHOST_SPAWN_EGG = ITEMS.register("ghost_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.GHOST, 0xe8e9ed, 0x98989d, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> HORSE_BAT_SPAWN_EGG = ITEMS.register("horse_bat_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.HORSE_BAT, 0x181818, 0xf3edbb, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> WILD_WOLF_SPAWN_EGG = ITEMS.register("wild_wolf_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.WILD_WOLF, 0x848589, 0x535251, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> NIGHTMARE_HORSE_SPAWN_EGG = ITEMS.register("nightmare_horse_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.NIGHTMARE_HORSE, 0x84412d, 0x947c6d, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> SKELETON_HORSE_SPAWN_EGG = ITEMS.register("skeleton_horse_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.SKELETON_HORSE_MOB, 0x68684f, 0xe5e5d8, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> ZOMBIE_HORSE_SPAWN_EGG = ITEMS.register("zombie_horse_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.ZOMBIE_HORSE_MOB, 0x315234, 0x97c284, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> FIRE_GHOST_SPAWN_EGG = ITEMS.register("fire_ghost_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.FIRE_GHOST, 0x9a3231, 0x641e1e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.GOBLIN, 0x5d8745, 0x554035, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> IMP_SPAWN_EGG = ITEMS.register("imp_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.IMP, 0x8b1a1a, 0xff6600, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> WEREVILLAGER_SPAWN_EGG = ITEMS.register("werevillager_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.WEREVILLAGER, 0x9e6e42, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> DART_GOBLIN_SPAWN_EGG = ITEMS.register("dart_goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.DART_GOBLIN, 0x4a7a35, 0xe8c840, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.SCORPION, 0x2a2e32, 0x2f3338, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> DESERT_SCORPION_SPAWN_EGG = ITEMS.register("desert_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.DESERT_SCORPION, 0xc0a35a, 0x443a23, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> FROST_SCORPION_SPAWN_EGG = ITEMS.register("frost_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.FROST_SCORPION, 0x245066, 0xffffff, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> NETHER_SCORPION_SPAWN_EGG = ITEMS.register("nether_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.NETHER_SCORPION, 0x240f10, 0xff5d1d, new Item.Properties().stacksTo(64)));

    /**
     * Materials
     */
    public static final Supplier<Item> HEAVY_CLAW = ITEMS.register("heavy_claw",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> LONG_FANG = ITEMS.register("long_fang",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> GIANT_FEATHER = ITEMS.register("giant_feather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> SCORPION_STING = ITEMS.register("scorpion_sting",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> CHITIN = ITEMS.register("chitin",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> SNOW_DUVET = ITEMS.register("snow_duvet",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<SmithingTemplateItem> SHRIKE_UPGRADE_SMITHING_TEMPLATE =
            ITEMS.register("shrike_upgrade_smithing_template", () -> new SmithingTemplateItem(
                    Component.translatable("item.fangs_n_claws.shrike_upgrade_smithing_template.applies_to")
                            .withStyle(net.minecraft.ChatFormatting.BLUE),
                    Component.translatable("item.fangs_n_claws.shrike_upgrade_smithing_template.ingredients")
                            .withStyle(net.minecraft.ChatFormatting.BLUE),
                    Component.translatable("upgrade.fangs_n_claws.shrike_upgrade")
                            .withStyle(net.minecraft.ChatFormatting.GRAY),
                    Component.translatable("item.fangs_n_claws.shrike_upgrade_smithing_template.base_slot_description"),
                    Component.translatable("item.fangs_n_claws.shrike_upgrade_smithing_template.additions_slot_description"),
                    java.util.List.of(
                            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet"),
                            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate"),
                            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings"),
                            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots")),
                    java.util.List.of(
                            ResourceLocation.withDefaultNamespace("item/empty_slot_diamond"))));

    public static final Supplier<Item> FUR = ITEMS.register("fur",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<VileFatItem> VILE_FAT = ITEMS.register("vile_fat", VileFatItem::new);

    public static final Supplier<Item> SPECTRAL_ESSENCE = ITEMS.register("spectral_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final Supplier<Item> BLACK_HORN = ITEMS.register("black_horn",
            () -> new Item(new Item.Properties().stacksTo(64)));

    /**
     * Weapons
     */
    public static final Supplier<BlowgunItem>           BLOWGUN            = ITEMS.register("blowgun",            BlowgunItem::new);
    public static final Supplier<FangDaggerItem>        FANG_DAGGER        = ITEMS.register("fang_dagger",        FangDaggerItem::new);
    public static final Supplier<NetheriteFangDaggerItem> NETHERITE_DAGGER = ITEMS.register("netherite_dagger",   NetheriteFangDaggerItem::new);
    public static final Supplier<CatchingClawItem>      CATCHING_CLAW      = ITEMS.register("catching_claw",      CatchingClawItem::new);
    public static final Supplier<NetheriteClawItem>     CATCHING_CLAW_NETHERITE = ITEMS.register("catching_claw_netherite", NetheriteClawItem::new);
    public static final Supplier<SilverSwordItem>       SILVER_SWORD       = ITEMS.register("silver_sword",       SilverSwordItem::new);
    public static final Supplier<FirePitchforkItem>     FIRE_PITCHFORK     = ITEMS.register("fire_pitchfork",    FirePitchforkItem::new);
    public static final Supplier<HellFirePitchforkItem> HELLFIRE_PITCHFORK = ITEMS.register("hellfire_pitchfork", HellFirePitchforkItem::new);

    /**
     * Armor
     */
    public static final Supplier<ScorpionArmorItem> SCORPION_HELMET     = ITEMS.register("scorpion_helmet",
            () -> new ScorpionArmorItem(ArmorItem.Type.HELMET,     150));
    public static final Supplier<ScorpionArmorItem> SCORPION_CHESTPLATE = ITEMS.register("scorpion_chestplate",
            () -> new ScorpionArmorItem(ArmorItem.Type.CHESTPLATE, 220));
    public static final Supplier<ScorpionArmorItem> SCORPION_LEGGINGS   = ITEMS.register("scorpion_leggings",
            () -> new ScorpionArmorItem(ArmorItem.Type.LEGGINGS,   200));
    public static final Supplier<ScorpionArmorItem> SCORPION_BOOTS      = ITEMS.register("scorpion_boots",
            () -> new ScorpionArmorItem(ArmorItem.Type.BOOTS,      175));

    public static final Supplier<FurArmorItem> FUR_HELMET     = ITEMS.register("fur_helmet",
            () -> new FurArmorItem(ArmorItem.Type.HELMET,     165));
    public static final Supplier<FurArmorItem> FUR_CHESTPLATE = ITEMS.register("fur_chestplate",
            () -> new FurArmorItem(ArmorItem.Type.CHESTPLATE, 240));
    public static final Supplier<FurArmorItem> FUR_LEGGINGS   = ITEMS.register("fur_leggings",
            () -> new FurArmorItem(ArmorItem.Type.LEGGINGS,   225));
    public static final Supplier<FurArmorItem> FUR_BOOTS      = ITEMS.register("fur_boots",
            () -> new FurArmorItem(ArmorItem.Type.BOOTS,      195));

    public static final Supplier<OwlArmorItem> OWL_HELMET     = ITEMS.register("owl_helmet",
            () -> new OwlArmorItem(ArmorItem.Type.HELMET,     165));
    public static final Supplier<OwlArmorItem> OWL_CHESTPLATE = ITEMS.register("owl_chestplate",
            () -> new OwlArmorItem(ArmorItem.Type.CHESTPLATE, 240));
    public static final Supplier<OwlArmorItem> OWL_LEGGINGS   = ITEMS.register("owl_leggings",
            () -> new OwlArmorItem(ArmorItem.Type.LEGGINGS,   225));
    public static final Supplier<OwlArmorItem> OWL_BOOTS      = ITEMS.register("owl_boots",
            () -> new OwlArmorItem(ArmorItem.Type.BOOTS,      195));

    public static final Supplier<ShrikeArmorItem> SHRIKE_HELMET     = ITEMS.register("shrike_helmet",
            () -> new ShrikeArmorItem(ArmorItem.Type.HELMET,     363));
    public static final Supplier<ShrikeArmorItem> SHRIKE_CHESTPLATE = ITEMS.register("shrike_chestplate",
            () -> new ShrikeArmorItem(ArmorItem.Type.CHESTPLATE, 528));
    public static final Supplier<ShrikeArmorItem> SHRIKE_LEGGINGS   = ITEMS.register("shrike_leggings",
            () -> new ShrikeArmorItem(ArmorItem.Type.LEGGINGS,   495));
    public static final Supplier<ShrikeArmorItem> SHRIKE_BOOTS      = ITEMS.register("shrike_boots",
            () -> new ShrikeArmorItem(ArmorItem.Type.BOOTS,      429));

    /**
     * Throwables / Projectiles
     */
    public static final Supplier<EvilEyeItem>       EVIL_EYE       = ITEMS.register("evil_eye",       EvilEyeItem::new);
    public static final Supplier<VelocityArrowItem> VELOCITY_ARROW = ITEMS.register("velocity_arrow", VelocityArrowItem::new);
    public static final Supplier<PoisonousDartItem> POISONOUS_DART = ITEMS.register("poisonous_dart", PoisonousDartItem::new);

    /**
     * Items
     */
    public static final Supplier<Item> STURDY_SADDLE = ITEMS.register("sturdy_saddle",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> HORSE_BLANKET = ITEMS.register("horse_blanket",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> TOTEM_OF_FROST = ITEMS.register("totem_of_frost",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /**
     * Blocks
     */
    public static final Supplier<BlockItem> GHOST_BLOCK = ITEMS.register("ghost_block",
            () -> new BlockItem(BlocksRegistry.GHOST_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> BEAR_TRAP = ITEMS.register("beartrap",
            () -> new BlockItem(BlocksRegistry.BEAR_TRAP.get(), new Item.Properties()));

    public static final Supplier<BlockItem> VILE_LANTERN  = ITEMS.register("vile_lantern",
            () -> new BlockItem(BlocksRegistry.VILE_LANTERN.get(), new Item.Properties()));

    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}
