package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.BlowgunItem;
import net.raptorzizi.fangs_n_claws.item.CatchingClawItem;
import net.raptorzizi.fangs_n_claws.item.EvilEyeItem;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteClawItem;
import net.raptorzizi.fangs_n_claws.item.NetheriteFangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.SilverSwordItem;
import net.raptorzizi.fangs_n_claws.item.VelocityArrowItem;
import net.raptorzizi.fangs_n_claws.item.VileFatItem;

public class ItemsRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FangsClawsMod.MOD_ID);

    /**
     * Spawn eggs
     */
    public static final RegistryObject<ForgeSpawnEggItem> OGRE_SPAWN_EGG = ITEMS.register("ogre_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.OGRE, 0x418e4e, 0x14422e, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> CAVE_OGRE_SPAWN_EGG = ITEMS.register("cave_ogre_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.CAVE_OGRE, 0x3a3230, 0x1a90c8, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> WEREWOLF_SPAWN_EGG = ITEMS.register("werewolf_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.WEREWOLF, 0x39363c, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> OWLBEAR_SPAWN_EGG = ITEMS.register("owlbear_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.OWLBEAR, 0x554030, 0x2d2625, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> SILVER_SKELETON_SPAWN_EGG = ITEMS.register("silver_skeleton_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SILVER_SKELETON, 0x7b7871, 0xc2c0be, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> GOLEM_SPAWN_EGG = ITEMS.register("golem_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GOLEM, 0x9b775b, 0x52a12f, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> EVIL_BAT_SPAWN_EGG = ITEMS.register("evil_bat_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.EVIL_BAT, 0x43372f, 0xe8d063, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> GHOST_SPAWN_EGG = ITEMS.register("ghost_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GHOST, 0xe8e9ed, 0x98989d, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GOBLIN, 0x5d8745, 0x554035, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> WEREVILLAGER_SPAWN_EGG = ITEMS.register("werevillager_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.WEREVILLAGER, 0x9e6e42, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> DART_GOBLIN_SPAWN_EGG = ITEMS.register("dart_goblin_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.DART_GOBLIN, 0x4a7a35, 0xe8c840, new Item.Properties().stacksTo(64)));

    /**
     * Materials
     */
    public static final RegistryObject<Item> HEAVY_CLAW = ITEMS.register("heavy_claw",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> LONG_FANG = ITEMS.register("long_fang",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> GIANT_FEATHER = ITEMS.register("giant_feather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<VileFatItem> VILE_FAT = ITEMS.register("vile_fat", VileFatItem::new);

    public static final RegistryObject<Item> SPECTRAL_ESSENCE = ITEMS.register("spectral_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    /**
     * Weapons
     */
    public static final RegistryObject<FangDaggerItem>          FANG_DAGGER             = ITEMS.register("fang_dagger",              FangDaggerItem::new);
    public static final RegistryObject<NetheriteFangDaggerItem> NETHERITE_DAGGER        = ITEMS.register("netherite_dagger",         NetheriteFangDaggerItem::new);
    public static final RegistryObject<CatchingClawItem>        CATCHING_CLAW           = ITEMS.register("catching_claw",            CatchingClawItem::new);
    public static final RegistryObject<NetheriteClawItem>       CATCHING_CLAW_NETHERITE = ITEMS.register("catching_claw_netherite",  NetheriteClawItem::new);
    public static final RegistryObject<SilverSwordItem>         SILVER_SWORD            = ITEMS.register("silver_sword",             SilverSwordItem::new);

    /**
     * Throwables / Projectiles
     */
    public static final RegistryObject<BlowgunItem>        BLOWGUN        = ITEMS.register("blowgun",        BlowgunItem::new);
    public static final RegistryObject<Item>               POISONOUS_DART = ITEMS.register("poisonous_dart", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<EvilEyeItem>       EVIL_EYE       = ITEMS.register("evil_eye",       EvilEyeItem::new);
    public static final RegistryObject<VelocityArrowItem> VELOCITY_ARROW = ITEMS.register("velocity_arrow", VelocityArrowItem::new);

    /**
     * Blocks
     */
    public static final RegistryObject<BlockItem> GHOST_BLOCK = ITEMS.register("ghost_block",
            () -> new BlockItem(BlocksRegistry.GHOST_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> BEAR_TRAP = ITEMS.register("beartrap",
            () -> new BlockItem(BlocksRegistry.BEAR_TRAP.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> VILE_LANTERN = ITEMS.register("vile_lantern",
            () -> new BlockItem(BlocksRegistry.VILE_LANTERN.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
