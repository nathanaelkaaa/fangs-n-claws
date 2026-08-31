package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class BabyPackFollowGoal extends Goal {

    private static final double SEARCH_RADIUS = 16.0;
    private static final double FOLLOW_START  = 5.0;
    private static final double STOP_DIST     = 2.5;
    private static final double SPEED         = 1.2;
    private static final int    REPATH        = 10;

    private final BabyWildWolfEntity baby;
    private WildWolfEntity adult;
    private int repathCooldown;

    public BabyPackFollowGoal(BabyWildWolfEntity baby) {
        this.baby = baby;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private WildWolfEntity findAdult() {
        Class<?> wanted = baby.adultClass();
        List<WildWolfEntity> nearby = baby.level().getEntitiesOfClass(WildWolfEntity.class,
                baby.getBoundingBox().inflate(SEARCH_RADIUS),
                w -> w.isAlive() && w.getClass() == wanted);
        return nearby.stream().min(Comparator.comparingDouble(baby::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (baby.isTame()) return false;

        WildWolfEntity parent = baby.getParent();
        this.adult = (parent != null && parent.isAlive()) ? parent : findAdult();
        return adult != null && baby.distanceToSqr(adult) > FOLLOW_START * FOLLOW_START;
    }

    @Override
    public boolean canContinueToUse() {
        return !baby.isTame() && adult != null && adult.isAlive()
                && baby.distanceToSqr(adult) > STOP_DIST * STOP_DIST;
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.adult = null;
        baby.getNavigation().stop();
    }

    @Override
    public void tick() {
        baby.getLookControl().setLookAt(adult, 10.0F, 10.0F);
        if (--repathCooldown <= 0) {
            repathCooldown = REPATH;
            baby.getNavigation().moveTo(adult, SPEED);
        }
    }
}
