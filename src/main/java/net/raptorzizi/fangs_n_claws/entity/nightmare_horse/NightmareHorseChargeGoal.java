package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class NightmareHorseChargeGoal extends Goal {

    private static final double ENGAGE_RANGE = 16.0;

    private final NightmareHorseEntity horse;

    public NightmareHorseChargeGoal(NightmareHorseEntity horse) {
        this.horse = horse;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (horse.isTamed() || horse.getControllingPassenger() instanceof Player) return false;
        if (horse.getChargeCooldown() != 0 || horse.getChargePhase() != NightmareHorseEntity.PHASE_IDLE) return false;
        LivingEntity t = horse.getTarget();
        return t != null && t.isAlive() && horse.distanceTo(t) <= ENGAGE_RANGE && horse.hasLineOfSight(t);
    }

    @Override
    public boolean canContinueToUse() {
        return horse.getChargePhase() != NightmareHorseEntity.PHASE_IDLE;
    }

    @Override
    public void start() {
        horse.startWindup();
    }

    @Override
    public void stop() {
        horse.endCharge();
        horse.getNavigation().stop();
    }

    @Override
    public void tick() {
        int phase = horse.getChargePhase();
        horse.getNavigation().stop();
        if (phase == NightmareHorseEntity.PHASE_WINDUP) {
            horse.tickWindup(horse.getTarget());
        } else if (phase == NightmareHorseEntity.PHASE_CHARGE) {
            horse.tickCharge();
        }
    }
}
