package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (mimickedState != null) {
            tag.put("MimickedState", NbtUtils.writeBlockState(mimickedState));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MimickedState")) {
            mimickedState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("MimickedState"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
