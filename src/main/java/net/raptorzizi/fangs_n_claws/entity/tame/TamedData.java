package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class TamedData {

    private static final String OWNER_KEY        = "Owner";
    private static final String LEGACY_OWNER_KEY = "OwnerUUID";
    private static final String SIT_KEY          = "Sitting";

    private TamedData() { }

    public static void save(CompoundTag tag, TamableCreature creature) {
        UUID owner = creature.getOwnerUUID();
        if (owner != null) tag.putUUID(OWNER_KEY, owner);
        if (creature.isOrderedToSit()) tag.putBoolean(SIT_KEY, true);
    }

    public static void load(CompoundTag tag, TamableCreature creature) {
        UUID owner = readOwner(tag);
        if (owner != null) creature.setOwnerUUID(owner);
        creature.setOrderedToSit(tag.getBoolean(SIT_KEY));
    }

    @Nullable
    public static UUID readOwner(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY))        return tag.getUUID(OWNER_KEY);
        if (tag.hasUUID(LEGACY_OWNER_KEY)) return tag.getUUID(LEGACY_OWNER_KEY);
        return null;
    }
}
