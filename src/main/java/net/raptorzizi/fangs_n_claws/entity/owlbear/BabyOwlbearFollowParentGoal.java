package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BabyOwlbearFollowParentGoal extends Goal {

    private static final double START_DIST = 6.0;
    private static final double STOP_DIST  = 3.0;
    private static final double SPEED      = 1.15;

    private final BabyOwlbearEntity baby;
    private OwlbearEntity parent;
    private int recalcTick;

    public BabyOwlbearFollowParentGoal(BabyOwlbearEntity baby) {
        this.baby = baby;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (baby.isTame()) return false;
        if (baby.isSleepPose()) return false;
        OwlbearEntity p = baby.getParent();
        if (p == null || !p.isAlive() || p.isSleeping()) return false;
        if (baby.distanceToSqr(p) < START_DIST * START_DIST) return false;
        this.parent = p;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !baby.isTame() && !baby.isSleepPose()
                && parent != null && parent.isAlive() && !parent.isSleeping()
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
