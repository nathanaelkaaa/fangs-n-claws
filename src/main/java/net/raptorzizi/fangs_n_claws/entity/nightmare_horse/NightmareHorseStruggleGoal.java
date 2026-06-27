package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class NightmareHorseStruggleGoal extends Goal {

    private static final double SPEED  = 2;
    private static final int    RADIUS = 8;

    private final NightmareHorseEntity horse;
    private double tx, ty, tz;

    public NightmareHorseStruggleGoal(NightmareHorseEntity horse) {
        this.horse = horse;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return horse.isStruggling() && findPos();
    }

    @Override
    public boolean canContinueToUse() {
        return horse.isStruggling();
    }

    @Override
    public void start() {
        horse.getNavigation().moveTo(tx, ty, tz, SPEED);
    }

    @Override
    public void stop() {
        horse.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (horse.getNavigation().isDone() && findPos()) {
            horse.getNavigation().moveTo(tx, ty, tz, SPEED);
        }
        if (horse.onGround() && horse.getRandom().nextInt(8) == 0) {
            horse.setDeltaMovement(horse.getDeltaMovement().add(0.0, 0.45, 0.0));
        }
    }

    private boolean findPos() {
        Vec3 pos = DefaultRandomPos.getPos(horse, RADIUS, 4);
        if (pos == null) return false;
        tx = pos.x;
        ty = pos.y;
        tz = pos.z;
        return true;
    }
}
