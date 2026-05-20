package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import java.util.EnumSet;

public class OwlbearFlyingAttackGoal extends Goal {

    // Variables

    private final OwlbearEntity owlbear;

    private static final double TARGET_HEIGHT_ABOVE     = 6.0;
    private static final double FLAP_THRESHOLD          = 1.5;
    private static final double MIN_HEIGHT_ABOVE_PLAYER = 2.5;
    private static final double FLY_SPEED               = 0.8;
    private static final int    FLAP_HYSTERESIS         = 8;

    private static final double DIVE_Y_THRESHOLD    = 5.0;
    private static final double DIVE_MAX_HORIZ_DIST = 8.0;
    private static final double DIVE_SPEED          = 1.0;
    private static final double DIVE_AOE_RADIUS     = 3.5;
    private static final float  DIVE_DAMAGE         = 10.0f;
    private static final int    DIVE_COOLDOWN       = 100;
    private static final int    SMOKE_COUNT         = 28;

    private int    flapHysteresisTicks = 0;
    private Vec3   lockedTargetPos     = null;
    private int    chargeTick          = 0;
    private int    landingTick         = 0;
    private int    diveCooldown        = 0;
    private double prevDiveVelY        = 0.0;

    // AI

    public OwlbearFlyingAttackGoal(OwlbearEntity owlbear) {
        this.owlbear = owlbear;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = owlbear.getTarget();
        if (target == null || !target.isAlive()) return false;
        return owlbear.distanceTo(target) > 15.0;
    }

    @Override
    public boolean canContinueToUse() {
        return owlbear.getDiveState() != OwlbearEntity.DIVE_NONE || owlbear.isFlying();
    }

    @Override
    public void start() {
        owlbear.setFlying(true);
        owlbear.setFlapping(true);
        owlbear.setRunning(false);
        owlbear.getNavigation().stop();
        flapHysteresisTicks = 0;
        prevDiveVelY = 0.0;
    }

    @Override
    public void stop() {
        owlbear.setFlying(false);
        owlbear.setFlapping(false);
        owlbear.setDiveState(OwlbearEntity.DIVE_NONE);
        flapHysteresisTicks = 0;
        diveCooldown = 0;
        lockedTargetPos = null;
        prevDiveVelY = 0.0;
    }

    @Override
    public void tick() {
        LivingEntity target = owlbear.getTarget();
        if (diveCooldown > 0) diveCooldown--;

        switch (owlbear.getDiveState()) {

            case OwlbearEntity.DIVE_NONE -> {
                if (target == null || !target.isAlive()) {
                    descentTick();
                    return;
                }
                owlbear.getNavigation().stop();

                double heightDiff = owlbear.getY() - target.getY();
                double horizDist  = Math.sqrt(Math.pow(owlbear.getX() - target.getX(), 2)
                                            + Math.pow(owlbear.getZ() - target.getZ(), 2));
                if (diveCooldown <= 0 && heightDiff > DIVE_Y_THRESHOLD
                        && horizDist <= DIVE_MAX_HORIZ_DIST && target.isAlive()) {
                    lockedTargetPos = new Vec3(target.getX(), target.getY(), target.getZ());
                    owlbear.setDiveState(OwlbearEntity.DIVE_CHARGING);
                    owlbear.setFlapping(false);
                    chargeTick = 0;
                    owlbear.playSound(SoundsRegistry.OWLBEAR_HOWL.get(),
                            1.2F, 0.85F + owlbear.getRandom().nextFloat() * 0.2F);
                } else {
                    normalFlightTick(target);
                }
            }

            case OwlbearEntity.DIVE_CHARGING -> {
                owlbear.getNavigation().stop();
                if (lockedTargetPos != null) {
                    owlbear.getLookControl().setLookAt(
                            lockedTargetPos.x, lockedTargetPos.y, lockedTargetPos.z, 30.0F, 30.0F);
                }
                owlbear.setDeltaMovement(owlbear.getDeltaMovement().scale(0.25));

                chargeTick++;
                if (chargeTick >= 5) {
                    owlbear.setDiveState(OwlbearEntity.DIVE_DIVING);
                }
            }

            case OwlbearEntity.DIVE_DIVING -> {
                owlbear.getNavigation().stop();
                owlbear.setNoGravity(true);
                if (lockedTargetPos != null) {
                    double survivedVelY = owlbear.getDeltaMovement().y;
                    boolean lostInertia = prevDiveVelY < -0.2 && survivedVelY > prevDiveVelY * 0.3;

                    Vec3 dir      = lockedTargetPos.subtract(owlbear.position());
                    Vec3 velocity = dir.normalize().scale(DIVE_SPEED);
                    owlbear.setDeltaMovement(velocity);
                    prevDiveVelY = velocity.y;

                    owlbear.setYRot(-((float) Mth.atan2(velocity.x, velocity.z)) * Mth.RAD_TO_DEG);
                    owlbear.yBodyRot = owlbear.getYRot();

                    if (owlbear.onGround() || lostInertia) {
                        triggerLanding();
                    }
                }
            }

            case OwlbearEntity.DIVE_LANDING -> {
                owlbear.setDeltaMovement(Vec3.ZERO);
                owlbear.getNavigation().stop();

                landingTick++;
                if (landingTick >= 5) {
                    diveCooldown = DIVE_COOLDOWN;
                    landingTick  = 0;
                    owlbear.setDiveState(OwlbearEntity.DIVE_NONE);
                }
            }
        }
    }

    private void normalFlightTick(LivingEntity target) {
        owlbear.getNavigation().stop();

        double targetX = target.getX();
        double targetY = target.getY() + TARGET_HEIGHT_ABOVE;
        double targetZ = target.getZ();

        owlbear.getLookControl().setLookAt(target, 30.0F, 30.0F);

        boolean tooLowFromTarget = (targetY - owlbear.getY()) > FLAP_THRESHOLD;
        boolean tooLowFromGround = owlbear.getY() < (target.getY() + MIN_HEIGHT_ABOVE_PLAYER);
        boolean wantsFlap = tooLowFromTarget || tooLowFromGround;

        if (wantsFlap != owlbear.isFlapping()) {
            flapHysteresisTicks++;
            if (flapHysteresisTicks >= FLAP_HYSTERESIS) {
                owlbear.setFlapping(wantsFlap);
                flapHysteresisTicks = 0;
            }
        } else {
            flapHysteresisTicks = 0;
        }

        double wantedY = owlbear.isFlapping() ? targetY : owlbear.getY();
        owlbear.getMoveControl().setWantedPosition(targetX, wantedY, targetZ, FLY_SPEED);
    }

    private void descentTick() {
        owlbear.setFlapping(false);
        owlbear.getNavigation().stop();
        if (owlbear.onGround()) {
            owlbear.setFlying(false);
        }
    }

    private void triggerLanding() {
        if (owlbear.getDiveState() == OwlbearEntity.DIVE_LANDING) return;
        owlbear.setFlying(false);
        owlbear.setDiveState(OwlbearEntity.DIVE_LANDING);
        owlbear.setDeltaMovement(Vec3.ZERO);
        landingTick = 0;

        if (!owlbear.level().isClientSide) {
            performDiveLanding();
        }
    }

    private void performDiveLanding() {
        ServerLevel serverLevel = (ServerLevel) owlbear.level();

        owlbear.playSound(
                SoundEvents.MACE_SMASH_GROUND,
                1.5F, 0.9F + owlbear.getRandom().nextFloat() * 0.2F);

        AABB aoe = new AABB(
                owlbear.getX() - DIVE_AOE_RADIUS, owlbear.getY() - 0.5, owlbear.getZ() - DIVE_AOE_RADIUS,
                owlbear.getX() + DIVE_AOE_RADIUS, owlbear.getY() + 2.0, owlbear.getZ() + DIVE_AOE_RADIUS);

        serverLevel.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != owlbear)
                .forEach(e -> {
                    e.hurt(owlbear.damageSources().mobAttack(owlbear), DIVE_DAMAGE);
                    e.addEffect(new MobEffectInstance(MobEffectsRegistry.STUNNED, 60, 0));
                });

        for (int i = 0; i < SMOKE_COUNT; i++) {
            double angle  = owlbear.getRandom().nextDouble() * 2.0 * Math.PI;
            double hSpeed = 0.15 + owlbear.getRandom().nextDouble() * 0.35;
            double vy     = 0.04 + owlbear.getRandom().nextDouble() * 0.18;
            double ox     = (owlbear.getRandom().nextDouble() - 0.5) * DIVE_AOE_RADIUS;
            double oz     = (owlbear.getRandom().nextDouble() - 0.5) * DIVE_AOE_RADIUS;

            serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    owlbear.getX() + ox,
                    owlbear.getY() + 0.1,
                    owlbear.getZ() + oz,
                    0,
                    Math.cos(angle) * hSpeed,
                    vy,
                    Math.sin(angle) * hSpeed,
                    1.0);
        }
    }
}
