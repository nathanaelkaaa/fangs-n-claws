package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PurpleGrubBurrowGoal extends Goal {

    private final PurpleGrubEntity grub;

    public PurpleGrubBurrowGoal(PurpleGrubEntity grub) {
        this.grub = grub;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return grub.isBurrowed();
    }

    @Override
    public boolean canContinueToUse() {
        return grub.isBurrowed();
    }

    @Override
    public void start() {
        grub.getNavigation().stop();
        grub.setTarget(null);
    }

    @Override
    public void tick() {
        grub.getNavigation().stop();
    }
}
