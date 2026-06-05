package net.raptorzizi.fangs_n_claws.entity.imp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ImpEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(ImpEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double FLOAT_HEIGHT         = 1.3;
    private static final double FLOAT_BOB_AMP       = 0.6;
    private static final double FLOAT_MAX_VERT       = 0.30;
    private static final double CHASE_SPEED          = 0.26;
    private static final double ORBIT_SPEED          = 0.24;
    private static final double FLEE_SPEED           = 0.22;
    private static final double ORBIT_RADIUS         = 3.5;
    private static final double ORBIT_ANGULAR_SPEED  = 0.020;
    private static final double DASH_SPEED           = 0.9;
    private static final double DASH_TRIGGER_RANGE   = 4.0;
    private static final int    DASH_LAUNCH_TICK     = 8;
    private static final int    DASH_TOTAL_TICKS     = 15;
    private static final int    DASH_COOLDOWN_MAX    = 55;
    private static final int    FLEE_TICKS           = 15;
    private static final int    ORBIT_DIR_CHANGE_MIN = 80;
    private static final int    ORBIT_DIR_CHANGE_MAX = 200;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM          = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM          = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM           = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation WALK_FLYING_ANIM   = RawAnimation.begin().thenLoop("walkFlying");
    private static final RawAnimation RUN_FLYING_ANIM    = RawAnimation.begin().thenLoop("runFlying");
    private static final RawAnimation ATTACK_FLYING_ANIM = RawAnimation.begin().then("attackFlying", Animation.LoopType.PLAY_ONCE);

    private LivingEntity pendingTarget      = null;
    private int          dashTick           = 0;
    private boolean      dashHasHit         = false;
    private int          dashCooldown       = 0;
    private int          fleeTick           = 0;
    private float        orbitAngle;
    private int          orbitDirection;
    private int          orbitDirChangeTick;

    public ImpEntity(EntityType<? extends ImpEntity> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemsRegistry.FIRE_PITCHFORK.get()));
        this.orbitAngle         = (float)(this.random.nextDouble() * Math.PI * 2.0);
        this.orbitDirection     = this.random.nextBoolean() ? 1 : -1;
        this.orbitDirChangeTick = ORBIT_DIR_CHANGE_MIN
                + this.random.nextInt(ORBIT_DIR_CHANGE_MAX - ORBIT_DIR_CHANGE_MIN);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    protected float getEquipmentDropChance(EquipmentSlot slot) {
        return 0f;
    }

    // Synced data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
    }

    public boolean isFlying()               { return this.entityData.get(IS_FLYING); }
    public void    setFlying(boolean value) { this.entityData.set(IS_FLYING, value); }

    // Spawn

    public static boolean checkImpSpawnRules(EntityType<? extends ImpEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level instanceof ServerLevel serverLevel) {
            int maxGroup = switch (level.getDifficulty()) {
                case EASY   -> 2;
                case NORMAL -> 3;
                default     -> 4;
            };
            long nearby = serverLevel.getEntitiesOfClass(
                    ImpEntity.class, new AABB(pos).inflate(24, 8, 24)).size();
            if (nearby >= maxGroup) return false;
        }
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               10.0)
                .add(Attributes.MOVEMENT_SPEED,            0.30)
                .add(Attributes.ATTACK_DAMAGE,             4.0)
                .add(Attributes.FOLLOW_RANGE,             24.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE,  1.5);
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Properties

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return !isFlying() && super.causeFallDamage(fallDistance, multiplier, source);
    }

    // Sound

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.GOBLIN_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.GOBLIN_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.GOBLIN_DEATH.get(); }
    @Override protected float     getSoundVolume()                { return 0.8F; }

    // Helpers

    public boolean isDashing() { return dashTick > 0; }

    private double findGroundY() {
        BlockPos base = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        for (int i = 0; i <= 10; i++) {
            BlockPos check = base.below(i);
            if (!this.level().getBlockState(check).isAir()) {
                return check.getY() + 1.0;
            }
        }
        return this.getY();
    }

    // Tick

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;
        if (dashCooldown > 0) dashCooldown--;
        if (dashTick > 0) {
            dashTick++;
            if (dashTick == DASH_LAUNCH_TICK && pendingTarget != null && pendingTarget.isAlive()) {
                double dx   = pendingTarget.getX() - this.getX();
                double dz   = pendingTarget.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.01) {
                    this.setDeltaMovement((dx / dist) * DASH_SPEED, 0.12, (dz / dist) * DASH_SPEED);
                }
                this.getNavigation().stop();
            }

            if (dashTick >= DASH_LAUNCH_TICK && !dashHasHit
                    && pendingTarget != null && pendingTarget.isAlive()) {
                if (this.getBoundingBox().inflate(0.15).intersects(pendingTarget.getBoundingBox())) {
                    super.doHurtTarget(pendingTarget);
                    FlamebrandEffect.addFlamebrandStack(pendingTarget);
                    dashHasHit    = true;
                    pendingTarget = null;
                }
            }

            if (dashTick >= DASH_TOTAL_TICKS) {
                dashTick    = 0;
                dashHasHit  = false;
                pendingTarget = null;
                fleeTick    = FLEE_TICKS;
            }
        }

        boolean hasCombatTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (hasCombatTarget != isFlying()) {
            setFlying(hasCombatTarget);
            if (!hasCombatTarget) {
                Vec3 cur = this.getDeltaMovement();
                if (cur.y < 0) this.setDeltaMovement(cur.x, 0, cur.z);
                this.resetFallDistance();
            }
        }
        this.setNoGravity(isFlying());
        if (isFlying()) this.resetFallDistance();
        if (isFlying()) tickFloat();

        tickCombat();
    }

    private void tickFloat() {
        double groundY = findGroundY();
        double bob     = Mth.sin(this.tickCount * 0.08f) * FLOAT_BOB_AMP;
        double hoverY  = groundY + FLOAT_HEIGHT + bob;
        double dy      = hoverY - this.getY();

        this.setDeltaMovement(this.getDeltaMovement().add(0, dy * 0.06, 0));

        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.12, 0));
        }

        Vec3 mov = this.getDeltaMovement();
        if (Math.abs(mov.y) > FLOAT_MAX_VERT) {
            this.setDeltaMovement(mov.x, Mth.clamp(mov.y, -FLOAT_MAX_VERT, FLOAT_MAX_VERT), mov.z);
        }
    }

    private void tickCombat() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        if (isDashing()) return;

        if (fleeTick > 0) {
            fleeTick--;
            Vec3 fleeDir    = this.position().subtract(target.position());
            Vec3 fleeTarget = this.position().add(
                    fleeDir.length() > 0.01 ? fleeDir.normalize().scale(1.5) : new Vec3(1, 0, 0));
            faceToward(fleeTarget);
            steerToward(fleeTarget, FLEE_SPEED, 0.22);
            return;
        }

        double dist = this.distanceTo(target);

        if (dashCooldown == 0 && dist <= DASH_TRIGGER_RANGE && dist >= 1.5) {
            triggerDash(target);
            dashCooldown = DASH_COOLDOWN_MAX;
        } else {
            tickOrbit(target);
        }
    }

    private void triggerDash(LivingEntity target) {
        this.triggerAnim("attack_controller", "attackFlying");
        this.pendingTarget = target;
        this.dashTick      = 1;
    }

    private void tickOrbit(LivingEntity target) {
        double dist = this.distanceTo(target);

        if (dist > ORBIT_RADIUS + 3.0) {
            faceToward(target.position());
            steerToward(target.position(), CHASE_SPEED, 0.20);
            return;
        }

        if (--orbitDirChangeTick <= 0) {
            orbitDirection      = -orbitDirection;
            orbitDirChangeTick  = ORBIT_DIR_CHANGE_MIN
                    + this.random.nextInt(ORBIT_DIR_CHANGE_MAX - ORBIT_DIR_CHANGE_MIN);
        }
        orbitAngle += (float)(ORBIT_ANGULAR_SPEED * orbitDirection);

        double ox = target.getX() + Math.cos(orbitAngle) * ORBIT_RADIUS;
        double oz = target.getZ() + Math.sin(orbitAngle) * ORBIT_RADIUS;

        faceToward(new Vec3(target.getX(), target.getY(), target.getZ()));
        steerToward(new Vec3(ox, target.getY(), oz), ORBIT_SPEED, 0.15);
    }

    private void steerToward(Vec3 target, double speed, double lerpFactor) {
        double dx    = target.x - this.getX();
        double dz    = target.z - this.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist > 0.2) {
            Vec3 cur = this.getDeltaMovement();
            double wx = (dx / hDist) * speed;
            double wz = (dz / hDist) * speed;
            this.setDeltaMovement(
                    cur.x + (wx - cur.x) * lerpFactor,
                    cur.y,
                    cur.z + (wz - cur.z) * lerpFactor
            );
        }
    }

    private void faceToward(Vec3 target) {
        double dx = target.x - this.getX();
        double dz = target.z - this.getZ();
        if (dx * dx + dz * dz < 0.01) return;
        float yaw = (float)(Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (isDashing()) return PlayState.STOP;

            boolean flying = isFlying();
            double  speed  = this.getDeltaMovement().horizontalDistance();

            if (flying && speed > 0.18) return state.setAndContinue(RUN_FLYING_ANIM);
            if (flying)                 return state.setAndContinue(WALK_FLYING_ANIM);
            if (speed > 0.18)           return state.setAndContinue(RUN_ANIM);
            if (state.isMoving())       return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attackFlying", ATTACK_FLYING_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
}
