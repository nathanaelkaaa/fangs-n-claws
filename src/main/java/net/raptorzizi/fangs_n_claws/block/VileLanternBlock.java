package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class VileLanternBlock extends LanternBlock {

    public VileLanternBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .forceSolidOn()
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .sound(SoundType.LANTERN)
                .lightLevel(state -> 15)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
    }
}
