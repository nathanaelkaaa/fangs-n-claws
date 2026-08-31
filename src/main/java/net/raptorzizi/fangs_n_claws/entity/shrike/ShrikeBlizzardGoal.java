package net.raptorzizi.fangs_n_claws.entity.shrike;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.EnumSet;

public class ShrikeBlizzardGoal extends Goal {

    private static final double APPROACH_DIST   = 4.0;
    private static final int    BLIZZARD_TICKS  = 60;
    private static final int    APPROACH_MAX    = 120;
    private static final int    COOLDOWN        = 200;
    private static final double FLY_SPEED       = 0.9;
    private static final double HOVER_ABOVE     = 2.5;

    private static final double PUSH_STRENGTH  = 0.12;
    private static final double BOX_LENGTH     = 20.0;
    private static final double BOX_HALF_WIDTH = 4.0;
    private static final double BOX_UP         = 3.0;
    private static final double BOX_DOWN       = 5.0;
    private static final float  BLIZZARD_DAMAGE = 3.0f;

    private final ShrikeEntity shrike;
    private LivingEntity target;
    private int phase;
    private int approachTick;
    private int blizzardTick;
    private long nextTime;

    public ShrikeBlizzardGoal(ShrikeEntity shrike) {
        this.shrike = shrike;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (shrike.level().getGameTime() < nextTime) return false;
        if (shrike.isSleeping()) return false;
        LivingEntity t = shrike.getTarget();
        if (t == null || !t.isAlive()) return false;
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) return false;
        if (phase == 0) return approachTick < APPROACH_MAX;
        return blizzardTick < BLIZZARD_TICKS;
    }

    @Override
    public void start() {
        phase = 0;
        approachTick = 0;
        blizzardTick = 0;
        shrike.setFlying(true);
        shrike.setFlapping(true);
        shrike.setRunning(false);
        shrike.getNavigation().stop();
    }

    @Override
    public void stop() {
        shrike.setBlizzard(false);
        shrike.setFlying(false);
        shrike.setFlapping(false);
        nextTime = shrike.level().getGameTime() + COOLDOWN;
        phase = 0;
        approachTick = 0;
        blizzardTick = 0;
        target = null;
    }

    @Override
    public void tick() {
        shrike.getNavigation().stop();
        shrike.getLookControl().setLookAt(target, 30.0F, 30.0F);
        shrike.setFlapping(true);

        float yaw = -((float) Mth.atan2(target.getX() - shrike.getX(),
                                        target.getZ() - shrike.getZ())) * Mth.RAD_TO_DEG;
        shrike.setYRot(yaw);
        shrike.yBodyRot = yaw;
        shrike.yHeadRot = yaw;

        double horiz = Math.sqrt(Math.pow(shrike.getX() - target.getX(), 2)
                               + Math.pow(shrike.getZ() - target.getZ(), 2));

        if (phase == 0) {
            approachTick++;
            shrike.getMoveControl().setWantedPosition(
                    target.getX(), target.getY() + HOVER_ABOVE, target.getZ(), FLY_SPEED);
            if (horiz <= APPROACH_DIST) {
                phase = 1;
                blizzardTick = 0;
                shrike.setBlizzard(true);
                shrike.playSound(SoundEvents.PLAYER_HURT_FREEZE, 1.4F, 0.7F);
            }
        } else {
            shrike.setBlizzard(true);
            shrike.getMoveControl().setWantedPosition(
                    shrike.getX(), target.getY() + HOVER_ABOVE, shrike.getZ(), 0.2);
            blizzardTick++;
            if (!shrike.level().isClientSide) applyBlizzard();
        }
    }

    private void applyBlizzard() {
        ServerLevel level = (ServerLevel) shrike.level();

        Vec3 look = shrike.getLookAngle();
        Vec3 wind = new Vec3(look.x, 0.0, look.z);
        if (wind.lengthSqr() < 1.0e-4) return;
        wind = wind.normalize();

        Vec3 right = new Vec3(-wind.z, 0.0, wind.x);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                shrike.getBoundingBox().inflate(BOX_LENGTH),
                e -> e != shrike && !FangsClawsMod.isFriendlyFire(shrike, e))) {
            double rx = e.getX() - shrike.getX();
            double ry = e.getY() - shrike.getY();
            double rz = e.getZ() - shrike.getZ();
            double forward = rx * wind.x  + rz * wind.z;
            double side    = rx * right.x + rz * right.z;
            if (forward < 0.0 || forward > BOX_LENGTH) continue;
            if (Math.abs(side) > BOX_HALF_WIDTH)        continue;
            if (ry < -BOX_DOWN || ry > BOX_UP)          continue;

            e.push(wind.x * PUSH_STRENGTH, 0.05, wind.z * PUSH_STRENGTH);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true, true));
            e.setTicksFrozen(Math.min(e.getTicksFrozen() + 16, e.getTicksRequiredToFreeze() + 40));
            e.hurt(shrike.damageSources().mobAttack(shrike), BLIZZARD_DAMAGE);
        }

        spawnBlizzardParticles(level, wind);
        level.playSound(null, shrike.getX(), shrike.getY(), shrike.getZ(),
                SoundEvents.WEATHER_RAIN, SoundSource.HOSTILE, 0.4F, 0.6F);
    }

    private void spawnBlizzardParticles(ServerLevel level, Vec3 wind) {
        RandomSource rng = shrike.getRandom();
        Vec3 right = new Vec3(-wind.z, 0.0, wind.x);

        for (int i = 0; i < 33; i++) {
            double forward = rng.nextDouble() * BOX_LENGTH;
            double side    = (rng.nextDouble() * 2.0 - 1.0) * BOX_HALF_WIDTH;
            double vert    = rng.nextDouble() * (BOX_UP + BOX_DOWN) - BOX_DOWN;

            double px = shrike.getX() + wind.x * forward + right.x * side;
            double py = shrike.getY() + vert;
            double pz = shrike.getZ() + wind.z * forward + right.z * side;

            double sp = 0.3 + rng.nextDouble() * 0.3;
            double vx = wind.x * sp + (rng.nextDouble() - 0.5) * 0.1;
            double vy = (rng.nextDouble() - 0.5) * 0.05;
            double vz = wind.z * sp + (rng.nextDouble() - 0.5) * 0.1;

            level.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 0, vx, vy, vz, 1.0);
        }
    }
}
