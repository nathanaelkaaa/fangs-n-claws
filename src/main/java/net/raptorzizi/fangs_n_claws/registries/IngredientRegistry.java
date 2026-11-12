package net.raptorzizi.fangs_n_claws.registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.amulet.EmptyAmuletIngredient;

public class IngredientRegistry {

    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<EmptyAmuletIngredient>>
            EMPTY_AMULET = INGREDIENT_TYPES.register("empty_amulet",
                    () -> new IngredientType<>(EmptyAmuletIngredient.CODEC));

    public static void register(IEventBus eventBus) {
        INGREDIENT_TYPES.register(eventBus);
    }
}
