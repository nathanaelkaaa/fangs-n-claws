package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                    .icon(() -> new ItemStack(Items.IRON_HOE))
                    .displayItems((enabledFeatures, entries) -> {
                        entries.accept(ItemsRegistry.OGRE_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
