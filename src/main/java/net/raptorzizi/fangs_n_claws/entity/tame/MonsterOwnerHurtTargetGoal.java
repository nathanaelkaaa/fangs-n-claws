package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public class MonsterOwnerHurtTargetGoal<T extends PathfinderMob & OwnedMonster> extends TargetGoal {

    private final T mob;
    private LivingEntity victim;
    private int lastAttackTimestamp;

    public MonsterOwnerHurtTargetGoal(T mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(java.util.EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!mob.isTamed() || mob.isOrderedToSit()) return false;
        LivingEntity owner = mob.getOwner();
        if (owner == null) return false;

        this.victim = owner.getLastHurtMob();
        int timestamp = owner.getLastHurtMobTimestamp();
        return timestamp != this.lastAttackTimestamp
                && canAttack(victim, TargetingConditions.DEFAULT)
                && victim != owner;
    }

    @Override
    public void start() {
        mob.setTarget(victim);
        LivingEntity owner = mob.getOwner();
        if (owner != null) this.lastAttackTimestamp = owner.getLastHurtMobTimestamp();
        super.start();
    }
}
