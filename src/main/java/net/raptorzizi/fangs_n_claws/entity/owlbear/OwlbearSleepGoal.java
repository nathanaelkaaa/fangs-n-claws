package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class OwlbearSleepGoal extends Goal {

    private final OwlbearEntity owlbear;

    private static final int MIN_IDLE_TICKS  = 200;
    private static final int SLEEP_TICKS_MIN = 1200;
    private static final int SLEEP_TICKS_MAX = 3600;
    private static final double WAKE_RADIUS = 3.0;
    private static final int SLEEP_CHANCE = 150;

    private int idleTicks    = 0;
    private int sleepTick    = 0;
    private int sleepDuration = 0;

    public OwlbearSleepGoal(OwlbearEntity owlbear) {
        this.owlbear = owlbear;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (owlbear.getTarget() != null) return false;
        if (owlbear.isAttacking())       return false;
        if (owlbear.isHowling())         return false;
        if (owlbear.isSleeping())        return false;

        if (!owlbear.getNavigation().isDone()) {
            idleTicks = 0;
            return false;
        }

        idleTicks++;
        if (idleTicks < MIN_IDLE_TICKS) return false;

        return owlbear.getRandom().nextInt(SLEEP_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (owlbear.getTarget() != null) return false;

        Player nearby = owlbear.level().getNearestPlayer(owlbear, WAKE_RADIUS);
        if (nearby != null && !nearby.isSpectator() && !nearby.isCreative() && !nearby.isCrouching()) return false;

        return sleepTick < sleepDuration;
    }

    @Override
    public void start() {
        sleepDuration = SLEEP_TICKS_MIN
                + owlbear.getRandom().nextInt(SLEEP_TICKS_MAX - SLEEP_TICKS_MIN + 1);
        sleepTick = 0;
        owlbear.setSleeping(true);
        owlbear.getNavigation().stop();
        owlbear.setRunning(false);
    }

    @Override
    public void tick() {
        sleepTick++;
        owlbear.getNavigation().stop();
        owlbear.setDeltaMovement(owlbear.getDeltaMovement().multiply(0, 1, 0));
    }

    @Override
    public void stop() {
        owlbear.setSleeping(false);
        idleTicks = 0;
    }
}
