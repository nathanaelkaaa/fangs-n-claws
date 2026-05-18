package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import java.util.function.Supplier;

public class ItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FangsClawsMod.MOD_ID);

    /**
     * Spawn eggs
     */
    public static final Supplier<DeferredSpawnEggItem> OGRE_SPAWN_EGG =  ITEMS.register("ogre_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.OGRE, 0x418e4e, 0x14422e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> WEREWOLF_SPAWN_EGG =  ITEMS.register("werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.WEREWOLF, 0x39363c, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> OWLBEAR_SPAWN_EGG =  ITEMS.register("owlbear_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.OWLBEAR, 0x554030, 0x2d2625, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> SILVER_SKELETON_SPAWN_EGG = ITEMS.register("silver_skeleton_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.SILVER_SKELETON, 0x7b7871, 0xc2c0be, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> GOLEM_SPAWN_EGG = ITEMS.register("golem_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.GOLEM, 0x9b775b, 0x52a12f, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> EVIL_BAT_SPAWN_EGG = ITEMS.register("evil_bat_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.EVIL_BAT, 0x43372f, 0xe8d063, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> GHOST_SPAWN_EGG = ITEMS.register("ghost_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.GHOST, 0xe8e9ed, 0x585858, new Item.Properties().stacksTo(64)));

    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}
