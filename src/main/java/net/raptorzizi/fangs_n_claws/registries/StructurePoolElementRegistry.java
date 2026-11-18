package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.biome.structure.GroundedSinglePoolElement;

public class StructurePoolElementRegistry {

    private static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<StructurePoolElementType<?>, StructurePoolElementType<GroundedSinglePoolElement>>
            GROUNDED_SINGLE = POOL_ELEMENTS.register("grounded_single_pool_element",
                    () -> () -> GroundedSinglePoolElement.CODEC);

    public static void register(IEventBus eventBus) {
        POOL_ELEMENTS.register(eventBus);
    }
}
