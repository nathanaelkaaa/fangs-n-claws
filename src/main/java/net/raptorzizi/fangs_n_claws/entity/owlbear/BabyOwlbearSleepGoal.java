package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BabyOwlbearSleepGoal extends Goal {

    private final BabyOwlbearEntity baby;

    public BabyOwlbearSleepGoal(BabyOwlbearEntity baby) {
        this.baby = baby;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean parentSleeping() {
        if (baby.isTame()) return false;
        OwlbearEntity parent = baby.getParent();
        return parent != null && parent.isAlive() && parent.isSleeping();
    }

    @Override
    public boolean canUse() {
        return parentSleeping();
    }

    @Override
    public boolean canContinueToUse() {
        return parentSleeping();
    }

    @Override
    public void start() {
        baby.setWildSleeping(true);
        baby.getNavigation().stop();
    }

    @Override
    public void tick() {
        baby.getNavigation().stop();
        baby.setDeltaMovement(baby.getDeltaMovement().multiply(0, 1, 0));
    }

    @Override
    public void stop() {
        baby.setWildSleeping(false);
    }
}
