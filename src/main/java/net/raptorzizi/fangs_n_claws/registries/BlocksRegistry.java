package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.block.BearTrapBlock;
import net.raptorzizi.fangs_n_claws.block.GhostBlock;
import net.raptorzizi.fangs_n_claws.block.MimicSpawnerBlock;
import net.raptorzizi.fangs_n_claws.block.VileLanternBlock;

public class BlocksRegistry {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<Block, GhostBlock> GHOST_BLOCK =
            BLOCKS.register("ghost_block", GhostBlock::new);

    public static final DeferredHolder<Block, BearTrapBlock> BEAR_TRAP =
            BLOCKS.register("beartrap", BearTrapBlock::new);

    public static final DeferredHolder<Block, VileLanternBlock> VILE_LANTERN =
            BLOCKS.register("vile_lantern", VileLanternBlock::new);

    public static final DeferredHolder<Block, MimicSpawnerBlock> MIMIC_SPAWNER =
            BLOCKS.register("mimic_spawner", () -> new MimicSpawnerBlock(
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .noLootTable()
                            .noOcclusion()
                            .replaceable()
                            .strength(0.0f)
                            .sound(SoundType.WOOD)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
