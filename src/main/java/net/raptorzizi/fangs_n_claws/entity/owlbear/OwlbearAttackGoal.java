package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class OwlbearAttackGoal extends Goal {

    // Variables

    private final OwlbearEntity owlbear;

    public static final double MIN_ATTACK_RANGE = 4.0;
    public static final double MAX_ATTACK_RANGE = 5.5;
    private static final double CHASE_SPEED     = 1.3;
    private static final int    ATTACK_INTERVAL = 15;

    private static final int HOWL_COOLDOWN         = 600;
    private static final int NO_PATH_FLY_THRESHOLD = 50;

    private int attackCooldown = 0;
    private int stuckTick      = 0;
    private int noPathTick     = 0;

    // AI

    public OwlbearAttackGoal(OwlbearEntity owlbear) {
        this.owlbear = owlbear;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (owlbear.isFlying()) return false;
        LivingEntity target = owlbear.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (owlbear.isFlying()) return false;
        LivingEntity target = owlbear.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void stop() {
        owlbear.setRunning(false);
        owlbear.getNavigation().stop();
        stuckTick  = 0;
        noPathTick = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = owlbear.getTarget();

        if (attackCooldown > 0) attackCooldown--;
        if (owlbear.howlCooldown > 0) owlbear.howlCooldown--;

        if (owlbear.isAttacking() || owlbear.isHowling()) {
            owlbear.setRunning(false);
            if (target != null) owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        if (target != null && target.isAlive()) {
            if (owlbear.howlCooldown <= 0) {
                owlbear.triggerHowl();
                owlbear.howlCooldown = HOWL_COOLDOWN;
            }

            if (owlbear.getNavigation().isStuck()) {
                stuckTick++;
                if (owlbear.onGround()) {
                    owlbear.getJumpControl().jump();
                }
                if (stuckTick % 20 == 0) {
                    owlbear.getNavigation().moveTo(target, CHASE_SPEED);
                }
            } else {
                stuckTick = 0;
            }

            owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double  distance = owlbear.distanceTo(target);
            boolean hasLos   = owlbear.hasLineOfSight(target);

            if (distance > MIN_ATTACK_RANGE || !hasLos) {
                owlbear.setRunning(true);
                owlbear.getNavigation().moveTo(target, CHASE_SPEED);
                if (!owlbear.getNavigation().isInProgress() || owlbear.getNavigation().isStuck()) {
                    if (++noPathTick >= NO_PATH_FLY_THRESHOLD) {
                        owlbear.forcedFlyMode = true;
                        noPathTick = 0;
                    }
                } else {
                    noPathTick = 0;
                }
            } else {
                owlbear.setRunning(false);
                owlbear.getNavigation().stop();
                noPathTick = 0;
                if (attackCooldown <= 0) {
                    attackCooldown = ATTACK_INTERVAL;
                    owlbear.doHurtTarget(target);
                }
            }
        }
    }
}
