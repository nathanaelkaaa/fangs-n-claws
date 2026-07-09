package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class HorseChargeGoal extends Goal {

    private final HorseMob      horse;
    private final ChargeAbility charge;

    public HorseChargeGoal(HorseMob horse, ChargeAbility charge) {
        this.horse = horse;
        this.charge = charge;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (horse.isTamed() || horse.getControllingPassenger() instanceof Player) return false;
        if (charge.cooldown() != 0 || charge.phase() != ChargeAbility.IDLE) return false;
        LivingEntity t = horse.getTarget();
        return t != null && t.isAlive() && horse.distanceTo(t) <= charge.engageRange && horse.hasLineOfSight(t);
    }

    @Override
    public boolean canContinueToUse() {
        return charge.phase() != ChargeAbility.IDLE;
    }

    @Override
    public void start() {
        charge.startWindup();
    }

    @Override
    public void stop() {
        charge.end();
        horse.getNavigation().stop();
    }

    @Override
    public void tick() {
        horse.getNavigation().stop();
        if (charge.phase() == ChargeAbility.WINDUP) {
            charge.tickWindup(horse.getTarget());
        } else if (charge.phase() == ChargeAbility.CHARGE) {
            charge.tickCharge();
        }
    }
}
