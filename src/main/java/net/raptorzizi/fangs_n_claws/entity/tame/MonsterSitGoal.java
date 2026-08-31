package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MonsterSitGoal<T extends PathfinderMob & OwnedMonster> extends Goal {

    private final T mob;

    public MonsterSitGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.isTamed() && mob.isOrderedToSit() && !mob.isInWaterOrBubble();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isTamed() && mob.isOrderedToSit();
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        mob.setTarget(null);
    }

    @Override
    public void tick() {
        mob.getNavigation().stop();
    }
}
