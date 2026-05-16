package net.raptorzizi.fangs_n_claws.entity.ogre;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class OgreAttackGoal extends Goal {

    // Variables

    private final OgreEntity ogre;

    public static final double MIN_ATTACK_RANGE = 3;
    public static final double MAX_ATTACK_RANGE = 5;
    private static final double CHASE_SPEED     = 1.4;
    private static final int ATTACK_INTERVAL    = 20;
    private static final int SEARCH_TIMEOUT  = 100;
    private static final int SIGHT_GRACE     = 10;

    private static final int SLAM_COMBAT_DELAY    = 60;
    private static final int STUCK_TICK_THRESHOLD = 40;
    private static final int SLAM_COOLDOWN        = 120;

    private int attackCooldown = 0;
    private int slamCooldown   = 0;
    private int noSightTick    = 0;
    private int combatTick     = 0;
    private int stuckTick      = 0;

    private Vec3 lastKnownPos = null;
    private int  searchTick   = 0;

    // AI

    public OgreAttackGoal(OgreEntity ogre) {
        this.ogre = ogre;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = ogre.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = ogre.getTarget();
        if (target != null && target.isAlive()) return true;
        return lastKnownPos != null && searchTick < SEARCH_TIMEOUT;
    }

    @Override
    public void stop() {
        ogre.setRunning(false);
        ogre.getNavigation().stop();
        lastKnownPos = null;
        searchTick   = 0;
        noSightTick  = 0;
        combatTick   = 0;
        stuckTick    = 0;
        slamCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = ogre.getTarget();

        if (attackCooldown > 0) attackCooldown--;
        if (slamCooldown   > 0) slamCooldown--;

        if (ogre.isAttacking() || ogre.isSlamming()) {
            ogre.setRunning(false);
            ogre.getNavigation().stop();
            if (target != null) ogre.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        if (target != null && target.isAlive()) {
            combatTick++;

            if (ogre.getNavigation().isStuck()) {
                stuckTick++;
            } else {
                stuckTick = 0;
            }

            if (slamCooldown <= 0) {
                boolean timeSlam  = combatTick >= SLAM_COMBAT_DELAY;
                boolean stuckSlam = stuckTick >= STUCK_TICK_THRESHOLD
                                    && !ogre.hasLineOfSight(target);

                if (timeSlam || stuckSlam) {
                    Vec3 toTarget = target.position().subtract(ogre.position());
                    Vec3 dir = toTarget.lengthSqr() > 0
                            ? toTarget.normalize()
                            : ogre.getLookAngle();
                    Vec3 impactPos = ogre.position().add(dir.x * 4.0, 0.0, dir.z * 4.0);
                    ogre.triggerSlam(impactPos);
                    slamCooldown = SLAM_COOLDOWN;
                    combatTick   = 0;
                    stuckTick    = 0;
                    return;
                }
            }

            if (ogre.hasLineOfSight(target)) {
                lastKnownPos = target.position();
                searchTick   = 0;
                noSightTick  = 0;
                ogre.getLookControl().setLookAt(target, 30.0F, 30.0F);

                double distance = ogre.distanceTo(target);
                if (distance > MAX_ATTACK_RANGE) {
                    ogre.setRunning(true);
                    ogre.getNavigation().moveTo(target, CHASE_SPEED);
                } else if (distance >= MIN_ATTACK_RANGE) {
                    ogre.setRunning(false);
                    ogre.getNavigation().stop();
                    if (attackCooldown <= 0) {
                        attackCooldown = ATTACK_INTERVAL;
                        ogre.doHurtTarget(target);
                    }
                } else {
                    ogre.setRunning(false);
                    ogre.getNavigation().stop();
                }
            } else {
                noSightTick++;
                ogre.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (noSightTick <= SIGHT_GRACE) {
                    ogre.setRunning(true);
                    if (ogre.getNavigation().isDone()) {
                        ogre.getNavigation().moveTo(target, CHASE_SPEED);
                    }
                } else if (lastKnownPos != null) {
                    searchAtLastKnownPos();
                } else {
                    ogre.setRunning(true);
                    if (ogre.getNavigation().isDone()) {
                        ogre.getNavigation().moveTo(target, CHASE_SPEED);
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
        ogre.setRunning(true);

        if (searchTick == 1 || ogre.getNavigation().isDone()) {
            ogre.getNavigation().moveTo(lastKnownPos.x, lastKnownPos.y, lastKnownPos.z, CHASE_SPEED);
        }

        double distToLastPos = ogre.position().distanceTo(lastKnownPos);
        if (distToLastPos < 2.0 || searchTick >= SEARCH_TIMEOUT) {
            lastKnownPos = null;
            searchTick   = 0;
            LivingEntity target = ogre.getTarget();
            if (target != null && target.isAlive()) {
                ogre.setRunning(true);
                ogre.getNavigation().moveTo(target, CHASE_SPEED);
            } else {
                ogre.setRunning(false);
                ogre.getNavigation().stop();
            }
        }
    }
}
