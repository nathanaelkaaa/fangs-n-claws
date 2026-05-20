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
    private static final int    ATTACK_INTERVAL       = 35;
    private static final double SLAM_RANGE            = 6.0;
    private static final int    STUCK_TICK_THRESHOLD  = 40;
    private static final int    SLAM_COOLDOWN         = 120;

    private int attackCooldown = 0;
    private int slamCooldown   = 0;
    private int stuckTick      = 0;

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
        return target != null && target.isAlive();
    }

    @Override
    public void stop() {
        ogre.setRunning(false);
        ogre.getNavigation().stop();
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
            if (ogre.getNavigation().isStuck()) {
                stuckTick++;
                if (ogre.onGround()) {
                    ogre.getJumpControl().jump();
                }
                if (stuckTick % 20 == 0) {
                    ogre.getNavigation().moveTo(target, CHASE_SPEED);
                }
            } else {
                stuckTick = 0;
            }

            if (slamCooldown <= 0) {
                double dist = ogre.distanceTo(target);
                boolean gapCloserSlam = dist > MAX_ATTACK_RANGE && dist <= SLAM_RANGE;
                boolean stuckSlam = stuckTick >= STUCK_TICK_THRESHOLD
                                    && !ogre.hasLineOfSight(target);

                if (gapCloserSlam || stuckSlam) {
                    Vec3 toTarget = target.position().subtract(ogre.position());
                    Vec3 dir = toTarget.lengthSqr() > 0
                            ? toTarget.normalize()
                            : ogre.getLookAngle();
                    Vec3 impactPos = ogre.position().add(dir.x * 4.0, 0.0, dir.z * 4.0);
                    ogre.triggerSlam(impactPos);
                    slamCooldown = SLAM_COOLDOWN;
                    stuckTick    = 0;
                    return;
                }
            }

            ogre.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distance = ogre.distanceTo(target);
            if (distance > MAX_ATTACK_RANGE) {
                ogre.setRunning(true);
                ogre.getNavigation().moveTo(target, CHASE_SPEED);
            } else if (distance >= MIN_ATTACK_RANGE) {
                if (attackCooldown <= 0) {
                    ogre.setRunning(false);
                    ogre.getNavigation().stop();
                    attackCooldown = ATTACK_INTERVAL;
                    ogre.doHurtTarget(target);
                } else {
                    ogre.setRunning(false);
                    if (ogre.getNavigation().isDone()) {
                        ogre.getNavigation().moveTo(target, CHASE_SPEED * 0.5);
                    }
                }
            } else {
                ogre.setRunning(false);
                ogre.getNavigation().stop();
            }
        }
    }
}
