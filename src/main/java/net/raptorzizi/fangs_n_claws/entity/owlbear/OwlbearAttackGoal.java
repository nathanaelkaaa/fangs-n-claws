package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class OwlbearAttackGoal extends Goal {

    private final OwlbearEntity owlbear;

    public static final double MIN_ATTACK_RANGE = 3.0;
    public static final double MAX_ATTACK_RANGE = 5.5;
    private static final double CHASE_SPEED     = 1.4;
    private static final int    ATTACK_INTERVAL = 20;

    private static final int SEARCH_TIMEOUT = 100;
    private static final int SIGHT_GRACE    = 10;
    private static final int HOWL_COOLDOWN  = 600;

    private int attackCooldown = 0;
    private int howlCooldown   = 0;
    private int noSightTick    = 0;

    private Vec3 lastKnownPos = null;
    private int  searchTick   = 0;

    public OwlbearAttackGoal(OwlbearEntity owlbear) {
        this.owlbear = owlbear;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = owlbear.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = owlbear.getTarget();
        if (target != null && target.isAlive()) return true;
        return lastKnownPos != null && searchTick < SEARCH_TIMEOUT;
    }

    @Override
    public void stop() {
        owlbear.setRunning(false);
        owlbear.getNavigation().stop();
        lastKnownPos  = null;
        searchTick    = 0;
        noSightTick   = 0;
        howlCooldown  = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = owlbear.getTarget();

        if (attackCooldown > 0) attackCooldown--;
        if (howlCooldown   > 0) howlCooldown--;

        if (owlbear.isAttacking() || owlbear.isHowling()) {
            owlbear.setRunning(false);
            owlbear.getNavigation().stop();
            if (target != null) owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        if (target != null && target.isAlive()) {
            if (howlCooldown <= 0) {
                owlbear.triggerHowl();
                howlCooldown = HOWL_COOLDOWN;
            }

            if (owlbear.hasLineOfSight(target)) {
                lastKnownPos = target.position();
                searchTick   = 0;
                noSightTick  = 0;
                owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);

                double distance = owlbear.distanceTo(target);
                if (distance > MIN_ATTACK_RANGE) {
                    owlbear.setRunning(true);
                    owlbear.getNavigation().moveTo(target, CHASE_SPEED);
                } else {
                    owlbear.setRunning(false);
                    owlbear.getNavigation().stop();
                    if (attackCooldown <= 0) {
                        attackCooldown = ATTACK_INTERVAL;
                        owlbear.doHurtTarget(target);
                    }
                }
            } else {
                noSightTick++;
                owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (noSightTick <= SIGHT_GRACE) {
                    owlbear.setRunning(true);
                    if (owlbear.getNavigation().isDone()) {
                        owlbear.getNavigation().moveTo(target, CHASE_SPEED);
                    }
                } else if (lastKnownPos != null) {
                    searchAtLastKnownPos();
                } else {
                    owlbear.setRunning(true);
                    if (owlbear.getNavigation().isDone()) {
                        owlbear.getNavigation().moveTo(target, CHASE_SPEED);
                    }
                }
            }
        } else {
            searchAtLastKnownPos();
        }
    }

    private void searchAtLastKnownPos() {
        if (lastKnownPos == null) return;

        searchTick++;
        owlbear.setRunning(true);

        if (searchTick == 1 || owlbear.getNavigation().isDone()) {
            owlbear.getNavigation().moveTo(lastKnownPos.x, lastKnownPos.y, lastKnownPos.z, CHASE_SPEED);
        }

        double distToLastPos = owlbear.position().distanceTo(lastKnownPos);
        if (distToLastPos < 2.0 || searchTick >= SEARCH_TIMEOUT) {
            lastKnownPos = null;
            searchTick   = 0;
            LivingEntity target = owlbear.getTarget();
            if (target != null && target.isAlive()) {
                owlbear.setRunning(true);
                owlbear.getNavigation().moveTo(target, CHASE_SPEED);
            } else {
                owlbear.setRunning(false);
                owlbear.getNavigation().stop();
            }
        }
    }
}
