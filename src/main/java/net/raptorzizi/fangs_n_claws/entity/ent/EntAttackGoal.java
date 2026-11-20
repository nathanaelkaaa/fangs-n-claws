package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class EntAttackGoal extends Goal {

    // Combat
    private static final double MELEE_RANGE = 3.5;
    private static final double CHASE_SPEED = 1.1;
    private static final int ATTACK_INTERVAL = 25;
    private static final int DIG_COOLDOWN = 160;
    private static final float DIG_CHANCE = 0.25F;
    private static final float LOOK_SPEED = 30.0F;

    private final EntEntity ent;
    private int attackCooldown;
    private int digCooldown;

    public EntAttackGoal(EntEntity ent) {
        this.ent = ent;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.ent.getTarget();
        return !this.ent.isDormant() && target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.ent.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.ent.getTarget();
        if (target == null) return;

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.digCooldown > 0) this.digCooldown--;

        this.ent.getLookControl().setLookAt(target, LOOK_SPEED, LOOK_SPEED);
        if (this.ent.isAttacking()) return;

        double distance = this.ent.distanceTo(target);
        if (distance > MELEE_RANGE || !this.ent.hasLineOfSight(target)) {
            this.ent.getNavigation().moveTo(target, CHASE_SPEED);
            return;
        }

        this.ent.getNavigation().stop();
        if (this.attackCooldown > 0) return;
        this.attackCooldown = ATTACK_INTERVAL;

        if (this.digCooldown <= 0 && this.ent.getRandom().nextFloat() < DIG_CHANCE) {
            this.digCooldown = DIG_COOLDOWN;
            this.ent.doDigAttack(target);
        } else {
            this.ent.doHurtTarget(target);
        }
    }
}
