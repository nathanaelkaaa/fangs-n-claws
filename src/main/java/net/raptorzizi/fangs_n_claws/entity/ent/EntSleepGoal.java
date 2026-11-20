package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class EntSleepGoal extends Goal {

    private final EntEntity ent;

    public EntSleepGoal(EntEntity ent) {
        this.ent = ent;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.ent.isDormant();
    }

    @Override
    public void start() {
        this.ent.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ent.getNavigation().stop();
        this.ent.setDeltaMovement(this.ent.getDeltaMovement().multiply(0.0, 1.0, 0.0));
    }
}
