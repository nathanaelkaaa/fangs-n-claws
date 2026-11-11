package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PurpleWormEggBlockEntity extends BlockEntity {

    private static final String OWNER_KEY = "Owner";

    @Nullable
    private UUID owner = null;

    public PurpleWormEggBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PURPLE_WORM_EGG_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) tag.putUUID(OWNER_KEY, owner);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(OWNER_KEY) ? tag.getUUID(OWNER_KEY) : null;
    }
}
