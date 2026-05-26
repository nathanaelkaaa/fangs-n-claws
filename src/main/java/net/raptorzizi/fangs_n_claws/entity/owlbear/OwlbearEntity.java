package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OwlbearEntity extends Monster implements GeoEntity {

    // Variables

    private static final int    ATTACK1_HIT_TICK  = 15;
    private static final int    ATTACK2_HIT_TICK  = 18;
    private static final int    ATTACK_TOTAL_TICKS = 25;
    private static final int    HOWL_TOTAL_TICKS   = 60;
    private static final double SPRINT_PARTICLE_SPEED_THRESHOLD = 0.05;
    private static final double SLEEP_DETECTION_RANGE           = 6.0;

    private static final EntityDataAccessor<Boolean>  IS_RUNNING  = SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean>  IS_SLEEPING = SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean>  IS_FLYING   = SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean>  IS_FLAPPING = SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer>  DIVE_STATE  = SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.INT);

    public static final int DIVE_NONE     = 0;
    public static final int DIVE_CHARGING = 1;
    public static final int DIVE_DIVING   = 2;
    public static final int DIVE_LANDING  = 3;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity  pendingAttackTarget  = null;
    private int     attackDelayTick      = 0;
    private int     currentAttackHitTick = ATTACK1_HIT_TICK;
    private boolean currentIsAttack1     = false;
    private int     howlDelayTick        = 0;

    int howlCooldown = 0;

    private double prevX, prevZ;
    private int    flapSoundTick = 0;
    private static final int FLAP_SOUND_INTERVAL = 12;

    private static final RawAnimation IDLE_ANIM          = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM          = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM           = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation SLEEP_ANIM         = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation FLY_HOVERING_ANIM  = RawAnimation.begin().thenLoop("fly_hovering");
    private static final RawAnimation FLY_FLAP_ANIM      = RawAnimation.begin().thenLoop("fly_flap");
    private static final RawAnimation FLY_DIVE_START_ANIM = RawAnimation.begin().then("fly_dive_attack_start", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation FLY_DIVE_END_ANIM   = RawAnimation.begin().then("fly_dive_attack_end",   Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK1_ANIM       = RawAnimation.begin().then("attack1", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK2_ANIM       = RawAnimation.begin().then("attack2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation HOWL_ANIM          = RawAnimation.begin().then("howl",    Animation.LoopType.PLAY_ONCE);

    // Spawn

    public OwlbearEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new OwlbearMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BetterPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_RUNNING,  false);
        this.entityData.define(IS_SLEEPING, false);
        this.entityData.define(IS_FLYING,   false);
        this.entityData.define(IS_FLAPPING, false);
        this.entityData.define(DIVE_STATE,  DIVE_NONE);
    }

    public boolean isRunning()                   { return this.entityData.get(IS_RUNNING); }
    public void    setRunning(boolean running)   { this.entityData.set(IS_RUNNING, running); }

    public boolean isSleeping()                  { return this.entityData.get(IS_SLEEPING); }
    public void    setSleeping(boolean sleeping) { this.entityData.set(IS_SLEEPING, sleeping); }

    public boolean isFlying() { return this.entityData.get(IS_FLYING); }
    public void    setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
        if (!this.level().isClientSide) {
            this.moveControl = flying ? new OwlbearFlyMoveControl(this) : new OwlbearMoveControl(this);
        }
    }

    public boolean isFlapping()                  { return this.entityData.get(IS_FLAPPING); }
    public void    setFlapping(boolean flapping) { this.entityData.set(IS_FLAPPING, flapping); }

    public int  getDiveState()          { return this.entityData.get(DIVE_STATE); }
    public void setDiveState(int state) { this.entityData.set(DIVE_STATE, state); }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OwlbearFlyingAttackGoal(this));
        this.goalSelector.addGoal(2, new OwlbearAttackGoal(this));
        this.goalSelector.addGoal(4, new OwlbearSleepGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false) {

            @Override
            public boolean canUse() {
                if (isSleeping()) {
                    Player nearby = level().getNearestPlayer(OwlbearEntity.this, SLEEP_DETECTION_RANGE);
                    if (nearby == null || nearby.isSpectator() || nearby.isCreative() || nearby.isCrouching())
                        return false;
                    this.target = nearby;
                    return true;
                }
                if (!super.canUse()) return false;
                return !(this.target instanceof Player p && p.isCrouching());
            }

            @Override
            public boolean canContinueToUse() {
                if (OwlbearEntity.this.getTarget() instanceof Player p && p.isCrouching()) return false;
                return super.canContinueToUse();
            }
        });
    }

    public static boolean checkOwlbearSpawnRules(EntityType<? extends OwlbearEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) return false;
        if (pos.getY() < level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1) return false;
        if (level instanceof ServerLevel serverLevel) {
            AABB area = new AABB(pos).inflate(48, 16, 48);
            if (!serverLevel.getEntitiesOfClass(OwlbearEntity.class, area).isEmpty()) return false;
        }
        return true;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               70.0)
                .add(Attributes.MOVEMENT_SPEED,            0.2)
                .add(Attributes.ATTACK_DAMAGE,             6.0)
                .add(Attributes.FOLLOW_RANGE,             24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,      0.5);
    }

    // Sound

    //@Override protected SoundEvent getAmbientSound()              { return SoundEvents.POLAR_BEAR_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.OWLBEAR_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.OWLBEAR_DEATH.get(); }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_STEP, 4.0F, 1.1F + this.random.nextFloat() * 0.2F);
    }

    // Combat

    public boolean isAttacking() { return attackDelayTick > 0; }
    public boolean isHowling()   { return howlDelayTick   > 0; }

    public void triggerHowl() {
        this.triggerAnim("attack_controller", "howl");
        this.howlDelayTick = 1;
        this.playSound(SoundsRegistry.OWLBEAR_HOWL.get(), 5.0F, 0.85F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        if (this.isFlying()) return false;
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.isFlying()) super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (this.random.nextInt(3) == 0) {
            this.triggerAnim("attack_controller", "attack1");
            this.currentAttackHitTick = ATTACK1_HIT_TICK;
            this.currentIsAttack1     = true;
        } else {
            this.triggerAnim("attack_controller", "attack2");
            this.currentAttackHitTick = ATTACK2_HIT_TICK;
            this.currentIsAttack1     = false;
        }
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    // Tick

    @Override
    public void tick() {
        prevX = this.getX();
        prevZ = this.getZ();

        super.tick();

        if (!this.level().isClientSide) {
            if (this.isFlying()) {
                this.setNoGravity(true);
                if (!this.isFlapping() && this.getDiveState() == DIVE_NONE) {
                    Vec3 mov = this.getDeltaMovement();
                    this.setDeltaMovement(mov.x, Math.max(mov.y - 0.072, -0.15), mov.z);
                }
            } else {
                this.setNoGravity(false);
            }
            if (attackDelayTick > 0) {
                attackDelayTick++;
                if (attackDelayTick == currentAttackHitTick) {
                    if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= OwlbearAttackGoal.MAX_ATTACK_RANGE
                            && this.hasLineOfSight(pendingAttackTarget)) {
                        if (currentIsAttack1 && pendingAttackTarget instanceof LivingEntity livingTarget) {
                            this.playSound(SoundsRegistry.OWLBEAR_SLASH.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
                            livingTarget.hurt(this.level().damageSources().mobAttack(this), 8.0f);
                            double dx = livingTarget.getX() - this.getX();
                            double dz = livingTarget.getZ() - this.getZ();
                            double len = Math.sqrt(dx * dx + dz * dz);
                            if (len > 0) {
                                livingTarget.knockback(3.5, -dx / len, -dz / len);
                            }
                        } else {
                            super.doHurtTarget(pendingAttackTarget);
                        }
                    }
                    pendingAttackTarget = null;
                }
                if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
            }

            if (howlDelayTick > 0) {
                howlDelayTick++;
                if (howlDelayTick >= HOWL_TOTAL_TICKS) howlDelayTick = 0;
            }

            if (this.isFlapping()) {
                flapSoundTick++;
                if (flapSoundTick >= FLAP_SOUND_INTERVAL) {
                    flapSoundTick = 0;
                    this.playSound(SoundsRegistry.OWLBEAR_FLAP.get(), 0.9F, 0.9F + this.random.nextFloat() * 0.2F);
                }
            } else {
                flapSoundTick = 0;
            }
        }

        if (this.level().isClientSide && this.onGround() && this.isRunning()) {
            double dx = this.getX() - prevX;
            double dz = this.getZ() - prevZ;
            if (Math.sqrt(dx * dx + dz * dz) > SPRINT_PARTICLE_SPEED_THRESHOLD) {
                spawnSprintBlockParticles();
            }
        }
    }

    private void spawnSprintBlockParticles() {
        BlockPos pos = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        BlockState state = this.level().getBlockState(pos);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    this.getX() + (this.random.nextFloat() - 0.5f) * this.getBbWidth(),
                    this.getBoundingBox().minY + 0.1,
                    this.getZ() + (this.random.nextFloat() - 0.5f) * this.getBbWidth(),
                    -this.getDeltaMovement().x * 4.0,
                    1.5,
                    -this.getDeltaMovement().z * 4.0
            );
        }
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 10, state -> {
            if (this.isFlying() || this.getDiveState() != DIVE_NONE) return PlayState.STOP;
            if (this.isSleeping()) return state.setAndContinue(SLEEP_ANIM);
            if (state.isMoving()) {
                if (this.isRunning()) return state.setAndContinue(RUN_ANIM);
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "flying", 3, state -> {
            int ds = this.getDiveState();
            if (ds == DIVE_CHARGING || ds == DIVE_DIVING) return state.setAndContinue(FLY_DIVE_START_ANIM);
            if (ds == DIVE_LANDING) return state.setAndContinue(FLY_DIVE_END_ANIM);
            if (!this.isFlying()) return PlayState.STOP;
            if (this.isFlapping()) return state.setAndContinue(FLY_FLAP_ANIM);
            return state.setAndContinue(FLY_HOVERING_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 3, state -> PlayState.STOP)
                .triggerableAnim("attack1", ATTACK1_ANIM)
                .triggerableAnim("attack2", ATTACK2_ANIM)
                .triggerableAnim("howl",    HOWL_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
