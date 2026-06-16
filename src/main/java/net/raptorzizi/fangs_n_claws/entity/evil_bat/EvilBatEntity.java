package net.raptorzizi.fangs_n_claws.entity.evil_bat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EvilBatEntity extends Monster implements GeoEntity {

    // Variables

    private static final EntityDataAccessor<Boolean> IS_RESTING =
            SynchedEntityData.defineId(EvilBatEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double WAKE_RANGE          = 6.0;
    private static final double CHASE_SPEED         = 0.50;
    private static final double RUSH_SPEED          = 0.95;
    private static final double RETREAT_SPEED       = 0.60;
    private static final double ARRIVE_THRESHOLD    = 1.8;
    private static final int    TARGET_MAX_COOLDOWN = 80;
    private static final int    SCAN_RADIUS         = 7;
    private static final int    ATTACK_HIT_TICK     = 6;
    private static final int    RUSH_MAX_TICKS      = 20;
    private static final int    RETREAT_TICKS       = 14;
    private static final int    ATTACK_COOLDOWN_MAX = 30;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Vec3   flyTarget      = null;
    private int    targetCooldown = 0;

    private int    rushTick       = 0;
    private int    retreatTick    = 0;
    private Vec3   retreatDir     = null;
    private int    attackCooldown = 0;

    private static final RawAnimation FLYING_ANIM  = RawAnimation.begin().thenLoop("flying");
    private static final RawAnimation RESTING_ANIM = RawAnimation.begin().thenLoop("resting");

    // Spawn

    public EvilBatEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_RESTING, true);
    }

    public boolean isResting()               { return this.entityData.get(IS_RESTING); }
    public void    setResting(boolean value) { this.entityData.set(IS_RESTING, value); }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,                6.0)
                .add(Attributes.MOVEMENT_SPEED,             0.0)
                .add(Attributes.ATTACK_DAMAGE,              3.0)
                .add(Attributes.FOLLOW_RANGE,              28.0)
                .add(Attributes.FLYING_SPEED,               0.28);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Sound, Death

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.BAT_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.BAT_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.BAT_DEATH; }
    @Override protected float getSoundVolume()                    { return 0.1F; }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.FLY_INTO_WALL) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    // Combat

    public boolean isAttacking() { return rushTick > 0 || retreatTick > 0; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && !isResting()) {
            Entity attacker = source.getDirectEntity();
            Vec3 away = attacker != null
                    ? this.position().subtract(attacker.position())
                    : new Vec3(0, 1, 0);
            retreatDir     = away.length() > 0.01 ? away.normalize() : new Vec3(0, 1, 0);
            rushTick       = 0;
            retreatTick    = 1;
            attackCooldown = ATTACK_COOLDOWN_MAX;
        }
        return result;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (isAttacking() || attackCooldown > 0) return false;
        rushTick    = 1;
        retreatTick = 0;
        retreatDir  = null;
        return true;
    }

    private void tickAttack(LivingEntity target) {
        if (rushTick > 0) {
            rushTick++;

            Vec3 rushTarget = new Vec3(
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ());
            faceToward(rushTarget);
            steerToward(rushTarget, RUSH_SPEED, 0.35);

            if (rushTick == ATTACK_HIT_TICK) {
                if (target.isAlive() && this.distanceTo(target) <= 2.0 + 1.0) {
                    super.doHurtTarget(target);
                }
                Vec3 away = this.position().subtract(target.position());
                retreatDir = away.length() > 0.01 ? away.normalize() : new Vec3(0, 1, 0);
            }

            if (rushTick > ATTACK_HIT_TICK) {
                rushTick    = 0;
                retreatTick = 1;
                attackCooldown = ATTACK_COOLDOWN_MAX;
            }

            if (rushTick > RUSH_MAX_TICKS) {
                rushTick   = 0;
                retreatTick = 1;
                if (retreatDir == null) retreatDir = new Vec3(0, 1, 0);
                attackCooldown = ATTACK_COOLDOWN_MAX;
            }

        } else if (retreatTick > 0) {
            retreatTick++;

            if (retreatDir != null) {
                Vec3 retreatTarget = this.position().add(retreatDir.scale(5.0));
                faceToward(retreatTarget);
                steerToward(retreatTarget, RETREAT_SPEED, 0.3);
            }

            if (retreatTick >= RETREAT_TICKS) {
                retreatTick = 0;
                retreatDir  = null;
            }
        }
    }

    // Tick

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isSunBurnTick()) {
            this.setSecondsOnFire(8);
        }

        super.tick();
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            if (attackCooldown > 0) attackCooldown--;
        }

        if (isResting()) {
            tickResting();
        } else {
            tickFlying();
        }
    }

    private void tickResting() {
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) return;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            setResting(false);
            this.noPhysics = false;
            flyTarget = null;
            return;
        }

        Player nearby = this.level().getNearestPlayer(this, WAKE_RANGE);
        if (nearby != null && !nearby.isSpectator() && !nearby.isCreative()) {
            setResting(false);
            this.noPhysics = false;
            this.setTarget(nearby);
            flyTarget      = null;
            targetCooldown = 0;
            return;
        }

        BlockPos above = this.blockPosition().above();
        if (!this.level().getBlockState(above).blocksMotion()) {
            setResting(false);
            this.noPhysics = false;
        }
    }

    private void tickFlying() {
        this.noPhysics = false;

        if (this.level().isClientSide) return;

        LivingEntity target = this.getTarget();

        if (isAttacking()) {
            if (target != null && target.isAlive()) {
                tickAttack(target);
            } else {
                rushTick    = 0;
                retreatTick = 0;
                retreatDir  = null;
            }
            return;
        }

        if (target != null && target.isAlive()) {
            flyTarget = new Vec3(
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ());

            faceToward(flyTarget);
            if (this.distanceTo(target) <= 2.0 && attackCooldown == 0) {
                doHurtTarget(target);
            } else if (this.hurtTime == 0) {
                steerToward(flyTarget, CHASE_SPEED, 0.15);
            }

        } else {
            targetCooldown--;
            if (flyTarget == null || targetCooldown <= 0
                    || this.position().distanceTo(flyTarget) < ARRIVE_THRESHOLD) {
                if (tryRest()) return;
                flyTarget      = pickRandomFlyTarget();
                targetCooldown = TARGET_MAX_COOLDOWN;
            }

            if (flyTarget != null) faceToward(flyTarget);
            steerToward(flyTarget, this.getAttributeValue(Attributes.FLYING_SPEED), 0.12);

            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.1, 0));
                flyTarget = null;
            }
        }
    }

    private void steerToward(Vec3 target, double speed, double lerpFactor) {
        if (target == null) return;
        Vec3 diff = target.subtract(this.position());
        if (diff.length() > 0.2) {
            Vec3 wanted = diff.normalize().scale(speed);
            this.setDeltaMovement(this.getDeltaMovement().lerp(wanted, lerpFactor));
        }
    }

    private void faceToward(Vec3 target) {
        double dx = target.x - this.getX();
        double dy = target.y - (this.getY() + this.getBbHeight() * 0.5);
        double dz = target.z - this.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist * hDist + dy * dy < 0.01) return;

        float yaw   = (float)(Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        float pitch = (float)(-Mth.atan2(dy, hDist) * Mth.RAD_TO_DEG);

        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    private boolean tryRest() {
        if (this.random.nextInt(30) != 0) return false;

        BlockPos pos = this.blockPosition();
        for (int dy = 0; dy <= SCAN_RADIUS; dy++) {
            BlockPos check   = pos.above(dy);
            BlockPos ceiling = check.above();
            if (this.level().getBlockState(check).isAir()
                    && this.level().getBlockState(ceiling).blocksMotion()) {
                this.setPos(
                        Math.floor(this.getX()) + 0.5,
                        ceiling.getY() - this.getBbHeight(),
                        Math.floor(this.getZ()) + 0.5);
                this.setDeltaMovement(Vec3.ZERO);
                setResting(true);
                flyTarget = null;
                return true;
            }
        }
        return false;
    }

    private Vec3 pickRandomFlyTarget() {
        double range = 8.0;
        double tx = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * range;
        double ty = this.getY() + (this.random.nextDouble() * 2.0 - 1.0) * (range * 0.5);
        double tz = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * range;

        ty = Math.max(this.level().getMinBuildHeight() + 2,
                Math.min(ty, this.level().getMaxBuildHeight() - 2));

        if (this.level().getBlockState(BlockPos.containing(tx, ty, tz)).blocksMotion()) return null;
        return new Vec3(tx, ty, tz);
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state", 3, state -> {
            if (this.isResting()) return state.setAndContinue(RESTING_ANIM);
            return state.setAndContinue(FLYING_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
