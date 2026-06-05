package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;


public class CreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TAB.register("spellbook_materials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + FangsClawsMod.MOD_ID + ".creative_tab"))
                    .icon(() -> new ItemStack(ItemsRegistry.SILVER_SWORD.get()))
                    .displayItems((enabledFeatures, entries) -> {
                        entries.accept(ItemsRegistry.OGRE_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.CAVE_OGRE_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.HELL_OGRE_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.WEREWOLF_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.OWLBEAR_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.SILVER_SKELETON_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.GOLEM_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.EVIL_BAT_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.GHOST_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.GOBLIN_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.DART_GOBLIN_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.IMP_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.WEREVILLAGER_SPAWN_EGG.get());
                        entries.accept(ItemsRegistry.POISONOUS_DART.get());
                        entries.accept(ItemsRegistry.HEAVY_CLAW.get());
                        entries.accept(ItemsRegistry.LONG_FANG.get());
                        entries.accept(ItemsRegistry.GIANT_FEATHER.get());
                        entries.accept(ItemsRegistry.VILE_FAT.get());
                        entries.accept(ItemsRegistry.SPECTRAL_ESSENCE.get());
                        entries.accept(ItemsRegistry.BLOWGUN.get());
                        entries.accept(ItemsRegistry.FANG_DAGGER.get());
                        entries.accept(ItemsRegistry.NETHERITE_DAGGER.get());
                        entries.accept(ItemsRegistry.CATCHING_CLAW.get());
                        entries.accept(ItemsRegistry.CATCHING_CLAW_NETHERITE.get());
                        entries.accept(ItemsRegistry.SILVER_SWORD.get());
                        entries.accept(ItemsRegistry.FIRE_PITCHFORK.get());
                        entries.accept(ItemsRegistry.HELLFIRE_PITCHFORK.get());
                        entries.accept(ItemsRegistry.EVIL_EYE.get());
                        entries.accept(ItemsRegistry.VELOCITY_ARROW.get());
                        entries.accept(ItemsRegistry.GHOST_BLOCK.get());
                        entries.accept(ItemsRegistry.VILE_LANTERN.get());
                        entries.accept(ItemsRegistry.BEAR_TRAP.get());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
