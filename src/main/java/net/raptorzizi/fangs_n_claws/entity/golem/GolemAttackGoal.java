package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class GolemAttackGoal extends Goal {

    // Variables

    private final GolemEntity golem;

    public static final double MIN_ATTACK_RANGE  = 3.0;
    public static final double MAX_ATTACK_RANGE  = 5.0;
    public static final double THROW_MAX_RANGE   = 14.0;
    private static final double WALK_SPEED       = 1.2;
    private static final int    ATTACK_INTERVAL  = 25;
    private static final int    THROW_COOLDOWN   = 100;
    private static final int    SEARCH_TIMEOUT   = 100;
    private static final int    SIGHT_GRACE      = 10;

    private int attackCooldown = 0;
    private int throwCooldown  = 0;
    private int noSightTick    = 0;
    private int searchTick     = 0;

    private Vec3 lastKnownPos = null;

    // AI

    public GolemAttackGoal(GolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.isSleeping() || golem.isWakingUp()) return false;
        int state = golem.getGolemState();
        if (state == GolemEntity.STATE_FALLING
                || state == GolemEntity.STATE_VULNERABLE
                || state == GolemEntity.STATE_GETTING_UP) return false;
        LivingEntity target = golem.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (golem.isSleeping() || golem.isWakingUp()) return false;
        int state = golem.getGolemState();
        if (state == GolemEntity.STATE_FALLING
                || state == GolemEntity.STATE_VULNERABLE
                || state == GolemEntity.STATE_GETTING_UP) return false;
        LivingEntity target = golem.getTarget();
        if (target != null && target.isAlive()) return true;
        return lastKnownPos != null && searchTick < SEARCH_TIMEOUT;
    }

    @Override
    public void stop() {
        golem.getNavigation().stop();
        lastKnownPos  = null;
        searchTick    = 0;
        noSightTick   = 0;
        throwCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = golem.getTarget();

        if (attackCooldown > 0) attackCooldown--;
        if (throwCooldown  > 0) throwCooldown--;

        if (golem.isAttacking()) {
            golem.getNavigation().stop();
            if (target != null) golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        if (target != null && target.isAlive()) {
            if (golem.hasLineOfSight(target)) {
                lastKnownPos = target.position();
                searchTick   = 0;
                noSightTick  = 0;
                golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

                double dist = golem.distanceTo(target);

                if (dist > MAX_ATTACK_RANGE) {
                    golem.getNavigation().moveTo(target, WALK_SPEED);
                    if (dist <= THROW_MAX_RANGE && throwCooldown <= 0 && !golem.isMissingHand()) {
                        throwCooldown = THROW_COOLDOWN;
                        golem.doThrowAttack(target);
                    }
                } else if (dist >= MIN_ATTACK_RANGE) {
                    golem.getNavigation().stop();
                    if (attackCooldown <= 0) {
                        attackCooldown = ATTACK_INTERVAL;
                        golem.doHurtTarget(target);
                    }
                } else {
                    golem.getNavigation().stop();
                }

            } else {
                noSightTick++;
                golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (noSightTick <= SIGHT_GRACE) {
                    if (golem.getNavigation().isDone()) {
                        golem.getNavigation().moveTo(target, WALK_SPEED);
                    }
                } else if (lastKnownPos != null) {
                    searchAtLastKnownPos();
                } else {
                    if (golem.getNavigation().isDone()) {
                        golem.getNavigation().moveTo(target, WALK_SPEED);
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

        if (searchTick == 1 || golem.getNavigation().isDone()) {
            golem.getNavigation().moveTo(
                    lastKnownPos.x, lastKnownPos.y, lastKnownPos.z, WALK_SPEED);
        }

        if (golem.position().distanceTo(lastKnownPos) < 2.0 || searchTick >= SEARCH_TIMEOUT) {
            lastKnownPos = null;
            searchTick   = 0;
            LivingEntity target = golem.getTarget();
            if (target != null && target.isAlive()) {
                golem.getNavigation().moveTo(target, WALK_SPEED);
            } else {
                golem.getNavigation().stop();
            }
        }
    }
}
