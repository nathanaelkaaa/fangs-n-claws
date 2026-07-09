package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseArmorMenu;

public class MenuTypeRegistry {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FangsClawsMod.MOD_ID);

    public static final RegistryObject<MenuType<HorseArmorMenu>> HORSE_ARMOR =
            MENUS.register("horse_armor", () -> IForgeMenuType.create(HorseArmorMenu::fromNetwork));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
