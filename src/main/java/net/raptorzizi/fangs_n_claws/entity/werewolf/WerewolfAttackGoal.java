package net.raptorzizi.fangs_n_claws.entity.werewolf;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WerewolfAttackGoal extends Goal {

    private final WerewolfEntity werewolf;

    public static final double MIN_ATTACK_RANGE = 2.0;
    public static final double MAX_ATTACK_RANGE = 4.0;
    private static final double CHASE_SPEED     = 2.5;
    private static final int ATTACK_INTERVAL    = 20;

    private static final int SEARCH_TIMEOUT  = 100;
    private static final int SIGHT_GRACE     = 10;

    private static final int BITE_COMBAT_DELAY = 40;
    private static final int BITE_COOLDOWN     = 100;

    private static final int HOWL_COOLDOWN = 600;

    private int attackCooldown = 0;
    private int biteCooldown   = 0;
    private int howlCooldown   = 0;
    private int noSightTick    = 0;
    private int combatTick     = 0;

    private Vec3 lastKnownPos = null;
    private int  searchTick   = 0;

    public WerewolfAttackGoal(WerewolfEntity werewolf) {
        this.werewolf = werewolf;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = werewolf.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = werewolf.getTarget();
        if (target != null && target.isAlive()) return true;
        return lastKnownPos != null && searchTick < SEARCH_TIMEOUT;
    }

    @Override
    public void stop() {
        werewolf.setRunning(false);
        werewolf.getNavigation().stop();
        lastKnownPos = null;
        searchTick   = 0;
        noSightTick  = 0;
        combatTick   = 0;
        biteCooldown = 0;
        howlCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = werewolf.getTarget();

        if (attackCooldown > 0) attackCooldown--;
        if (biteCooldown   > 0) biteCooldown--;
        if (howlCooldown   > 0) howlCooldown--;

        if (werewolf.isAttacking() || werewolf.isBiting() || werewolf.isHowling()) {
            werewolf.setRunning(false);
            werewolf.getNavigation().stop();
            if (target != null) werewolf.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        if (target != null && target.isAlive()) {
            combatTick++;
            if (howlCooldown <= 0) {
                werewolf.triggerHowl();
                howlCooldown = HOWL_COOLDOWN;
            }

            if (biteCooldown <= 0 && combatTick >= BITE_COMBAT_DELAY) {
                werewolf.triggerBite(target);
                biteCooldown = BITE_COOLDOWN;
                combatTick   = 0;
                return;
            }

            if (werewolf.hasLineOfSight(target)) {
                lastKnownPos = target.position();
                searchTick   = 0;
                noSightTick  = 0;
                werewolf.getLookControl().setLookAt(target, 30.0F, 30.0F);

                double distance = werewolf.distanceTo(target);
                if (distance > MIN_ATTACK_RANGE) {
                    werewolf.setRunning(true);
                    werewolf.getNavigation().moveTo(target, CHASE_SPEED);
                } else {
                    werewolf.setRunning(false);
                    werewolf.getNavigation().stop();
                    if (attackCooldown <= 0) {
                        attackCooldown = ATTACK_INTERVAL;
                        werewolf.doHurtTarget(target);
                    }
                }
            } else {
                noSightTick++;
                werewolf.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (noSightTick <= SIGHT_GRACE) {
                    werewolf.setRunning(true);
                    if (werewolf.getNavigation().isDone()) {
                        werewolf.getNavigation().moveTo(target, CHASE_SPEED);
                    }
                } else if (lastKnownPos != null) {
                    searchAtLastKnownPos();
                } else {
                    werewolf.setRunning(true);
                    if (werewolf.getNavigation().isDone()) {
                        werewolf.getNavigation().moveTo(target, CHASE_SPEED);
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
        werewolf.setRunning(true);

        if (searchTick == 1 || werewolf.getNavigation().isDone()) {
            werewolf.getNavigation().moveTo(lastKnownPos.x, lastKnownPos.y, lastKnownPos.z, CHASE_SPEED);
        }

        double distToLastPos = werewolf.position().distanceTo(lastKnownPos);
        if (distToLastPos < 2.0 || searchTick >= SEARCH_TIMEOUT) {
            lastKnownPos = null;
            searchTick   = 0;
            LivingEntity target = werewolf.getTarget();
            if (target != null && target.isAlive()) {
                werewolf.setRunning(true);
                werewolf.getNavigation().moveTo(target, CHASE_SPEED);
            } else {
                werewolf.setRunning(false);
                werewolf.getNavigation().stop();
            }
        }
    }
}
