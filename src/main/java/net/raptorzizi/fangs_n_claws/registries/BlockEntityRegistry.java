package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.block.GhostBlockEntity;

public class BlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FangsClawsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<GhostBlockEntity>> GHOST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ghost_block",
                    () -> BlockEntityType.Builder
                            .of(GhostBlockEntity::new, BlocksRegistry.GHOST_BLOCK.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
