package net.raptorzizi.fangs_n_claws.entity.shrike;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ShrikeFeatherAttackGoal extends Goal {

    private static final int    WINDUP      = 8;
    private static final int    COOLDOWN    = 50;
    private static final double MIN_DIST    = 3.0;
    private static final double MAX_DIST    = 22.0;

    private final ShrikeEntity shrike;
    private LivingEntity target;
    private int windup;
    private boolean released;
    private long nextAttackTime;

    public ShrikeFeatherAttackGoal(ShrikeEntity shrike) {
        this.shrike = shrike;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (shrike.isFlying() || shrike.isSleeping() || shrike.isAttacking() || shrike.isHowling()) return false;
        if (shrike.level().getGameTime() < nextAttackTime) return false;
        LivingEntity t = shrike.getTarget();
        if (t == null || !t.isAlive()) return false;
        double d = shrike.distanceTo(t);
        if (d < MIN_DIST || d > MAX_DIST) return false;
        if (!shrike.hasLineOfSight(t)) return false;
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !released && target != null && target.isAlive() && !shrike.isFlying();
    }

    @Override
    public void start() {
        shrike.getNavigation().stop();
        shrike.setRunning(false);
        shrike.triggerAnim("attack_controller", "attack1");
        windup = WINDUP;
        released = false;
    }

    @Override
    public void tick() {
        shrike.getNavigation().stop();
        shrike.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (!released && --windup <= 0) {
            shrike.shootFeathers(target);
            released = true;
            nextAttackTime = shrike.level().getGameTime() + COOLDOWN;
        }
    }
}
