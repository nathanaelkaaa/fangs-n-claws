package net.raptorzizi.fangs_n_claws.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.LootTable;
import net.raptorzizi.fangs_n_claws.block.MimicSpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @Inject(
            method = "createChest(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("RETURN")
    )
    private void fnc$markMimicChest(ServerLevelAccessor level, BoundingBox box, RandomSource random,
                                    BlockPos pos, ResourceKey<LootTable> lootTable, BlockState state,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            MimicSpawnerBlock.scheduleAbove(level, pos);
        }
    }
}
