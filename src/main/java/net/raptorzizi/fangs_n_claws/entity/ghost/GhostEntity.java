package net.raptorzizi.fangs_n_claws.entity.ghost;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostEntity extends PathfinderMob implements GeoEntity {

    // Variables

    private static final EntityDataAccessor<Boolean> IS_ANGRY =
            SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double HOVER_HEIGHT     = 1.2;
    private static final double HOVER_MIN        = 0.6;
    private static final double HOVER_MAX        = 3.5;
    private static final double GROUND_SCAN_DIST = 12;
    private static final double FLY_SPEED        = 0.14;
    private static final double CHASE_SPEED      = 0.2;
    private static final double ARRIVE_THRESHOLD = 1.5;
    private static final int    TARGET_COOLDOWN  = 80;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Vec3 flyTarget      = null;
    private int  targetCooldown = 0;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM  = RawAnimation.begin().thenLoop("run");

    // Spawn

    public GhostEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ANGRY, false);
    }

    public boolean isAngry()               { return this.entityData.get(IS_ANGRY); }
    public void    setAngry(boolean value) { this.entityData.set(IS_ANGRY, value); }

    public static boolean checkGhostSpawnRules(EntityType<? extends GhostEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) return false;
        int brightness = level.getLevel().isThundering()
                ? level.getMaxLocalRawBrightness(pos, 10)
                : level.getMaxLocalRawBrightness(pos);
        if (brightness > random.nextInt(8)) return false;
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,                8.0)
                .add(Attributes.MOVEMENT_SPEED,             0.0)
                .add(Attributes.ATTACK_DAMAGE,              2.0)
                .add(Attributes.FOLLOW_RANGE,              20.0)
                .add(Attributes.FLYING_SPEED,               FLY_SPEED)
                .add(Attributes.ENTITY_INTERACTION_RANGE,   1.8);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    // Sound, Death

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.WARDEN_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.WARDEN_DEATH; }
    @Override protected float getSoundVolume()                    { return 0.3F; }

    // Combat

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        return super.doHurtTarget(target);
    }

    // Tick

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isSunBurnTick()) {
            this.igniteForSeconds(8);
        }

        super.tick();
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            boolean angry = this.getTarget() != null && this.getTarget().isAlive();
            if (angry != isAngry()) setAngry(angry);

            var slowFall = this.getEffect(MobEffects.SLOW_FALLING);
            if (slowFall == null || slowFall.getDuration() < 20) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
            }
        }

        tickMovement();
    }

    private void tickMovement() {
        if (this.level().isClientSide) return;

        LivingEntity target = this.getTarget();

        this.noPhysics = target != null && target.isAlive();
        double groundY      = findGroundY();

        double bob    = Mth.sin(this.tickCount * 0.08f) * 0.4;
        double hoverY = groundY + HOVER_HEIGHT + bob;

        if (target != null && target.isAlive()) {
            double targetY = target.getY() + target.getBbHeight() * 0.5;

            flyTarget = new Vec3(target.getX(), targetY, target.getZ());
            faceToward(flyTarget);

            if (this.distanceTo(target) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) {
                doHurtTarget(target);
            } else if (this.hurtTime == 0) {
                steerToward(flyTarget, CHASE_SPEED, 0.18);
            }

        } else {
            targetCooldown--;
            if (flyTarget == null || targetCooldown <= 0
                    || this.position().distanceTo(flyTarget) < ARRIVE_THRESHOLD) {
                flyTarget      = pickRoamTarget(hoverY);
                targetCooldown = TARGET_COOLDOWN;
            }

            if (flyTarget != null) {
                faceToward(flyTarget);
                steerToward(flyTarget, this.getAttributeValue(Attributes.FLYING_SPEED), 0.10);
            }

            double dy = hoverY - this.getY();
            this.setDeltaMovement(this.getDeltaMovement().add(0, dy * 0.06, 0));

            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.12, 0));
                flyTarget = null;
            }
        }
    }

    private double findGroundY() {
        BlockPos pos = this.blockPosition();
        for (int dy = 0; dy <= GROUND_SCAN_DIST; dy++) {
            BlockPos check = pos.below(dy);
            if (!this.level().getBlockState(check).isAir()) {
                return check.getY() + 1.0;
            }
        }
        return this.getY() - HOVER_HEIGHT;
    }

    private Vec3 pickRoamTarget(double hoverY) {
        double range = 10.0;
        double tx = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * range;
        double tz = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * range;
        double ty = hoverY + (this.random.nextDouble() * 2.0 - 1.0) * 1.0;

        ty = Mth.clamp(ty,
                this.level().getMinBuildHeight() + 2.0,
                this.level().getMaxBuildHeight() - 2.0);

        if (this.level().getBlockState(BlockPos.containing(tx, ty, tz)).blocksMotion()) return null;
        return new Vec3(tx, ty, tz);
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

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state", 5, state -> {
            if (isAngry()) return state.setAndContinue(RUN_ANIM);
            double speed = this.getDeltaMovement().horizontalDistance();
            if (speed > 0.05) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
