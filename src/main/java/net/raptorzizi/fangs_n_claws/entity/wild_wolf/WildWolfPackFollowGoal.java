package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class WildWolfPackFollowGoal extends Goal {

    private static final double FOLLOW_START = 4.0;
    private static final double STOP_DIST    = 2.0;
    private static final double SPEED        = 1.1;
    private static final int    REPATH       = 10;

    private final WildWolfEntity wolf;
    private WildWolfEntity leader;
    private int repathCooldown;

    public WildWolfPackFollowGoal(WildWolfEntity wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (wolf.isTamed()) return false;
        if (wolf.getTarget() != null) return false;
        WildWolfEntity l = wolf.getPackLeader();
        if (!isValidLeader(l)) return false;
        this.leader = l;
        return wolf.distanceToSqr(l) > FOLLOW_START * FOLLOW_START;
    }

    @Override
    public boolean canContinueToUse() {
        if (wolf.isTamed()) return false;
        if (wolf.getTarget() != null) return false;
        if (!isValidLeader(leader)) return false;
        if (wolf.getPackLeader() != leader) return false;
        return wolf.distanceToSqr(leader) > STOP_DIST * STOP_DIST;
    }

    @Override
    public void start() {
        repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.leader = null;
        wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        wolf.getLookControl().setLookAt(leader, 10.0F, 10.0F);
        if (--repathCooldown <= 0) {
            repathCooldown = REPATH;
            wolf.getNavigation().moveTo(leader, SPEED);
        }
    }

    private boolean isValidLeader(WildWolfEntity l) {
        return l != null && l.isAlive() && !l.isRemoved();
    }
}
