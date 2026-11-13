package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class MapDecorationRegistry {

    private static final int PIT_MAP_COLOR = 0x7A4A8C;

    private static final DeferredRegister<MapDecorationType> MAP_DECORATIONS =
            DeferredRegister.create(Registries.MAP_DECORATION_TYPE, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<MapDecorationType, MapDecorationType> PURPLE_WORM_PIT =
            MAP_DECORATIONS.register("purple_worm_pit", () -> new MapDecorationType(
                    FangsClawsMod.id("purple_worm_pit"),
                    true,
                    PIT_MAP_COLOR,
                    true,
                    false));

    public static void register(IEventBus eventBus) {
        MAP_DECORATIONS.register(eventBus);
    }
}
