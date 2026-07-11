package net.raptorzizi.fangs_n_claws.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.raptorzizi.fangs_n_claws.block.MimicSpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * Les donjons (monster rooms) posent leur coffre via {@code Feature.safeSetBlock} plutot que par une
 * piece de structure. On intercepte donc ces poses : si le bloc pose est un coffre, on marque
 * l'emplacement au-dessus pour un mimic potentiel. Voir {@link MimicSpawnerBlock}.
 */
@Mixin(Feature.class)
public class FeatureMixin {

    @Inject(
            method = "safeSetBlock(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/function/Predicate;)V",
            at = @At("HEAD")
    )
    private void fnc$markMimicChest(WorldGenLevel level, BlockPos pos, BlockState state,
                                    Predicate<BlockState> oldState, CallbackInfo ci) {
        if (state.is(Blocks.CHEST)) {
            MimicSpawnerBlock.scheduleAbove(level, pos);
        }
    }
}
