package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BabyScorpionFollowParentGoal extends Goal {

    private static final double START_DIST = 6.0;
    private static final double STOP_DIST  = 2.5;
    private static final double SPEED      = 1.2;

    private final BabyScorpionEntity baby;
    private ScorpionEntity parent;
    private int recalcTick;

    public BabyScorpionFollowParentGoal(BabyScorpionEntity baby) {
        this.baby = baby;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (baby.isPassenger() || baby.isOnHead()) return false;
        ScorpionEntity p = baby.getParent();
        if (p == null || !p.isAlive()) return false;
        if (baby.distanceToSqr(p) < START_DIST * START_DIST) return false;
        this.parent = p;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !baby.isPassenger() && !baby.isOnHead()
                && parent != null && parent.isAlive()
                && baby.distanceToSqr(parent) > STOP_DIST * STOP_DIST;
    }

    @Override
    public void start() {
        recalcTick = 0;
    }

    @Override
    public void stop() {
        parent = null;
        baby.getNavigation().stop();
    }

    @Override
    public void tick() {
        baby.getLookControl().setLookAt(parent, 10.0F, (float) baby.getMaxHeadXRot());
        if (--recalcTick <= 0) {
            recalcTick = 10;
            baby.getNavigation().moveTo(parent, SPEED);
        }
    }
}
