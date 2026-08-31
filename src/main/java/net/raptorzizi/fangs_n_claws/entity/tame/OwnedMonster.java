package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.OwnableEntity;

public interface OwnedMonster extends OwnableEntity {

    boolean isTamed();

    boolean isOrderedToSit();

    void setOrderedToSit(boolean sit);
}
