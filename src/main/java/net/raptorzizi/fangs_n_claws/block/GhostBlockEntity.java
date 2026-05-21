package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import org.jetbrains.annotations.Nullable;

public class GhostBlockEntity extends BlockEntity {

    @Nullable
    private BlockState mimickedState = null;

    public GhostBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GHOST_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public BlockState getMimickedState() {
        return mimickedState;
    }

    public void setMimickedState(@Nullable BlockState state) {
        this.mimickedState = state;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (mimickedState != null) {
            tag.put("MimickedState", NbtUtils.writeBlockState(mimickedState));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MimickedState")) {
            mimickedState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("MimickedState"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
