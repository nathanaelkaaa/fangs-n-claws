package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WildWolfPanicGoal extends Goal {

    private static final double SPEED  = 1.4;
    private static final int    RADIUS = 8;

    private final WildWolfEntity wolf;
    private double tx, ty, tz;

    public WildWolfPanicGoal(WildWolfEntity wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return wolf.isPanicking() && findFleePos();
    }

    @Override
    public boolean canContinueToUse() {
        return wolf.isPanicking();
    }

    @Override
    public void start() {
        wolf.getNavigation().moveTo(tx, ty, tz, SPEED);
    }

    @Override
    public void stop() {
        wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (wolf.getNavigation().isDone() && findFleePos()) {
            wolf.getNavigation().moveTo(tx, ty, tz, SPEED);
        }
    }

    private boolean findFleePos() {
        Vec3 pos = DefaultRandomPos.getPos(wolf, RADIUS, 4);
        if (pos == null) return false;
        tx = pos.x;
        ty = pos.y;
        tz = pos.z;
        return true;
    }
}
