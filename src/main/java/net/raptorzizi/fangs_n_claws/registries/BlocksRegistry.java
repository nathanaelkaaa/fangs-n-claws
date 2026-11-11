package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.block.AcidBlock;
import net.raptorzizi.fangs_n_claws.block.BearTrapBlock;
import net.raptorzizi.fangs_n_claws.block.GhostBlock;
import net.raptorzizi.fangs_n_claws.block.MimicSpawnerBlock;
import net.raptorzizi.fangs_n_claws.block.PointedVermiciteBlock;
import net.raptorzizi.fangs_n_claws.block.PurpleWormEggBlock;
import net.raptorzizi.fangs_n_claws.block.VileLanternBlock;

public class BlocksRegistry {

    private static final int ACID_LIGHT_LEVEL = 15;
    private static final float ACID_DESTROY_TIME = 100.0F;

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<Block, GhostBlock> GHOST_BLOCK =
            BLOCKS.register("ghost_block", GhostBlock::new);

    public static final DeferredHolder<Block, BearTrapBlock> BEAR_TRAP =
            BLOCKS.register("beartrap", BearTrapBlock::new);

    public static final DeferredHolder<Block, VileLanternBlock> VILE_LANTERN =
            BLOCKS.register("vile_lantern", VileLanternBlock::new);

    public static final DeferredHolder<Block, PurpleWormEggBlock> PURPLE_WORM_EGG =
            BLOCKS.register("purple_worm_egg", PurpleWormEggBlock::new);

    public static final DeferredHolder<Block, MimicSpawnerBlock> MIMIC_SPAWNER =
            BLOCKS.register("mimic_spawner", () -> new MimicSpawnerBlock(
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .noLootTable()
                            .noOcclusion()
                            .replaceable()
                            .strength(0.0f)
                            .sound(SoundType.WOOD)));


    public static final DeferredHolder<Block, Block> VERMICITE =
            BLOCKS.register("vermicite", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .sound(SoundType.CALCITE)
                            .requiresCorrectToolForDrops()
                            .strength(0.75F)));

    public static final DeferredHolder<Block, PointedVermiciteBlock> POINTED_VERMICITE =
            BLOCKS.register("pointed_vermicite", () -> new PointedVermiciteBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .forceSolidOn()
                            .noOcclusion()
                            .sound(SoundType.POINTED_DRIPSTONE)
                            .strength(1.5F, 3.0F)
                            .dynamicShape()
                            .offsetType(BlockBehaviour.OffsetType.XZ)
                            .pushReaction(PushReaction.DESTROY)
                            .isRedstoneConductor((state, getter, pos) -> false)));


    public static final DeferredHolder<Block, AcidBlock> ACID =
            BLOCKS.register("acid", () -> new AcidBlock(
                    FluidRegistry.FLOWING_ACID.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .replaceable()
                            .noCollission()
                            .strength(ACID_DESTROY_TIME)
                            .lightLevel(state -> ACID_LIGHT_LEVEL)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
