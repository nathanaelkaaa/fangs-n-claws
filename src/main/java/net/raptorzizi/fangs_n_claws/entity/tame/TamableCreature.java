package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface TamableCreature extends OwnableEntity {

    boolean isTamed();

    void setOwnerUUID(@Nullable UUID uuid);

    default boolean isOwnedBy(Player player) {
        return player.getUUID().equals(getOwnerUUID());
    }

    default boolean isOrderedToSit() { return false; }

    default void setOrderedToSit(boolean sit) { }
}
