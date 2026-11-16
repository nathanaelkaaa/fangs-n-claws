package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.biome.structure.FluidDrainProcessor;

public class StructureProcessorRegistry {

    private static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<FluidDrainProcessor>>
            FLUID_DRAIN = PROCESSORS.register("fluid_drain",
                    () -> () -> FluidDrainProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        PROCESSORS.register(eventBus);
    }
}
