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
            () -> new DeferredSpawnEggItem(EntityRegistry.OGRE, 0xb6a783, 0x89a349, new Item.Properties().stacksTo(64)));

    public static final Supplier<DeferredSpawnEggItem> WEREWOLF_SPAWN_EGG =  ITEMS.register("werewolf_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityRegistry.WEREWOLF, 0x39363c, 0xe42e2e, new Item.Properties().stacksTo(64)));

    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}
