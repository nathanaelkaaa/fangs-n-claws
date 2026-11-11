package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.block.GhostBlockEntity;
import net.raptorzizi.fangs_n_claws.block.PurpleWormEggBlockEntity;

public class BlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhostBlockEntity>> GHOST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ghost_block",
                    () -> BlockEntityType.Builder
                            .of(GhostBlockEntity::new, BlocksRegistry.GHOST_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PurpleWormEggBlockEntity>> PURPLE_WORM_EGG_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("purple_worm_egg",
                    () -> BlockEntityType.Builder
                            .of(PurpleWormEggBlockEntity::new, BlocksRegistry.PURPLE_WORM_EGG.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
