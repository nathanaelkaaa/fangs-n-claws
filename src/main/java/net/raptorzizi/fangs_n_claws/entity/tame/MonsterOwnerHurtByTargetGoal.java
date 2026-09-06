package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public class MonsterOwnerHurtByTargetGoal<T extends PathfinderMob & TamableCreature> extends TargetGoal {

    private final T mob;
    private LivingEntity attacker;
    private int lastAttackTimestamp;

    public MonsterOwnerHurtByTargetGoal(T mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(java.util.EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!mob.isTamed() || mob.isOrderedToSit()) return false;
        LivingEntity owner = mob.getOwner();
        if (owner == null) return false;

        this.attacker = owner.getLastHurtByMob();
        int timestamp = owner.getLastHurtByMobTimestamp();
        return timestamp != this.lastAttackTimestamp
                && canAttack(attacker, TargetingConditions.DEFAULT)
                && attacker != owner;
    }

    @Override
    public void start() {
        mob.setTarget(attacker);
        LivingEntity owner = mob.getOwner();
        if (owner != null) this.lastAttackTimestamp = owner.getLastHurtByMobTimestamp();
        super.start();
    }
}
