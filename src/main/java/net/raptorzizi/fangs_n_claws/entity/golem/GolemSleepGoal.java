package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;


public class GolemSleepGoal extends Goal {

    private final GolemEntity golem;

    private static final double WAKE_RADIUS = GolemEntity.SLEEP_DETECTION_RANGE;

    public GolemSleepGoal(GolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return golem.isSleeping() && !golem.isWakingUp();
    }

    @Override
    public boolean canContinueToUse() {
        if (!golem.isSleeping()) return false;

        Player nearby = golem.level().getNearestPlayer(golem, WAKE_RADIUS);
        if (nearby != null && !nearby.isSpectator() && !nearby.isCreative()) {
            golem.setTarget(nearby);
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        golem.getNavigation().stop();
        golem.setDeltaMovement(golem.getDeltaMovement().multiply(0, 1, 0));
    }

    @Override
    public void stop() {
        golem.startWakingUp();
    }
}
