package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseArmorMenu;

public class MenuTypeRegistry {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<HorseArmorMenu>> HORSE_ARMOR =
            MENUS.register("horse_armor", () -> IMenuTypeExtension.create(HorseArmorMenu::fromNetwork));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
