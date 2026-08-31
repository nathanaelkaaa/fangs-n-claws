package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CarnivorousPlantEntity extends Monster implements GeoEntity {

    private static final int STATE_HIDDEN    = 0;
    private static final int STATE_REVEALING = 1;
    private static final int STATE_ACTIVE    = 2;
    private static final int STATE_HIDING    = 3;

    private static final int REVEAL_ANIM_TICKS = 10;
    private static final int HIDE_ANIM_TICKS   = 10;

    private static final double ATTACK_RANGE = 3.0;
    private static final double HIT_RANGE    = 3.5;
    private static final double STAY_RANGE   = 4.5;
    private static final int    HIDE_DELAY_TICKS = 60;

    private static final int ATTACK_TOTAL_TICKS = 15;
    private static final int ATTACK_HIT_TICK    = 11;

    private static final double LOOK_RANGE = 8.0;
    private static final float  BODY_EASE  = 0.2f;

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(CarnivorousPlantEntity.class, EntityDataSerializers.INT);

    private int animTimer   = 0;
    private int awayTimer   = 0;
    private int attackTimer = 0;

    private float lockedYaw = 0.0f;
    private Double anchorX = null, anchorZ = null;

    private float bodyYaw, bodyYawO;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HIDE_ANIM   = RawAnimation.begin().then("hide",   Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation REVEAL_ANIM = RawAnimation.begin().then("reveal", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);

    public CarnivorousPlantEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor level, @NotNull MobSpawnType spawnType) {
        if (level instanceof ServerLevelAccessor sla && sla.getDifficulty() == Difficulty.PEACEFUL) return false;
        BlockPos pos = this.blockPosition();
        if (level.getBrightness(LightLayer.SKY, pos) <= 0) return false;
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           20.0)
                .add(Attributes.MOVEMENT_SPEED,        0.0)
                .add(Attributes.ATTACK_DAMAGE,         5.0)
                .add(Attributes.FOLLOW_RANGE,          8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  1.0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            protected double getFollowDistance() { return LOOK_RANGE; }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, STATE_HIDDEN);
    }

    public int  getState()          { return this.entityData.get(STATE); }
    private void setState(int state) { this.entityData.set(STATE, state); }

    public boolean isHidden()    { return getState() == STATE_HIDDEN; }
    public boolean isDeployed()  { return getState() == STATE_ACTIVE || getState() == STATE_REVEALING; }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.lockedYaw = Math.round(this.getYRot() / 90.0f) * 90.0f;
        this.applyLockedYaw();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    private void applyLockedYaw() {
        this.setYRot(this.lockedYaw);
        this.yRotO      = this.lockedYaw;
        this.yBodyRot   = this.lockedYaw;
        this.yBodyRotO  = this.lockedYaw;
        this.yHeadRot   = this.lockedYaw;
        this.yHeadRotO  = this.lockedYaw;
        this.setXRot(0.0f);
        this.xRotO = 0.0f;
    }

    // Immobilité

    @Override public void knockback(double strength, double x, double z) {}
    @Override public void push(double x, double y, double z)             {}
    @Override public boolean isPushable()                                { return false; }
    @Override public boolean isPushedByFluid()                           { return false; }
    @Override public boolean ignoreExplosion(@NotNull Explosion explosion) { return true; }

    @Override
    public void tick() {
        super.tick();
        this.applyLockedYaw();

        if (!this.level().isClientSide) {
            Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(0.0, dm.y, 0.0);

            if (this.anchorX == null) {
                this.anchorX = this.getX();
                this.anchorZ = this.getZ();
            } else if (this.getX() != this.anchorX || this.getZ() != this.anchorZ) {
                this.setPos(this.anchorX, this.getY(), this.anchorZ);
            }
        }

        this.tickBodyTracking();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickStateMachine();
        this.tickAttack();
    }

    // Hide / reveal

    @Nullable
    private LivingEntity getLookTarget() {
        LivingEntity t = this.getTarget();
        if (t != null && t.isAlive()) return t;
        return this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), LOOK_RANGE, false);
    }

    private boolean preyWithin(double range) {
        LivingEntity t = this.getLookTarget();
        return t != null && t.isAlive() && this.distanceTo(t) <= range;
    }

    private void tickStateMachine() {
        switch (getState()) {
            case STATE_HIDDEN -> {
                if (preyWithin(ATTACK_RANGE)) {
                    setState(STATE_REVEALING);
                    animTimer = 0;
                }
            }
            case STATE_REVEALING -> {
                if (++animTimer >= REVEAL_ANIM_TICKS) setState(STATE_ACTIVE);
            }
            case STATE_ACTIVE -> {
                if (preyWithin(STAY_RANGE)) {
                    awayTimer = 0;
                } else if (++awayTimer >= HIDE_DELAY_TICKS && attackTimer == 0) {
                    setState(STATE_HIDING);
                    animTimer = 0;
                }
            }
            case STATE_HIDING -> {
                if (++animTimer >= HIDE_ANIM_TICKS) {
                    setState(STATE_HIDDEN);
                    awayTimer = 0;
                }
            }
            default -> { }
        }
    }

    // Attack

    public boolean isAttacking() { return attackTimer > 0; }

    private void tickAttack() {
        if (attackTimer > 0) {
            attackTimer++;
            if (attackTimer == ATTACK_HIT_TICK) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive() && this.distanceTo(target) <= HIT_RANGE) {
                    super.doHurtTarget(target);
                }
            }
            if (attackTimer > ATTACK_TOTAL_TICKS) attackTimer = 0;
            return;
        }

        if (getState() != STATE_ACTIVE) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceTo(target) > ATTACK_RANGE) return;

        this.triggerAnim("attack_controller", "attack");
        attackTimer = 1;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        return false;
    }

    // Body Tracking

    private void tickBodyTracking() {
        this.bodyYawO = this.bodyYaw;

        float wantYaw = 0.0f;
        if (this.isDeployed()) {
            LivingEntity target = this.getLookTarget();
            if (target != null && target.isAlive()) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                float desiredYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
                wantYaw = -Mth.wrapDegrees(desiredYaw - this.lockedYaw);
            }
        }
        this.bodyYaw = Mth.wrapDegrees(this.bodyYaw + Mth.wrapDegrees(wantYaw - this.bodyYaw) * BODY_EASE);
    }

    public float getBodyYawOffset(float partialTick) {
        return Mth.rotLerp(partialTick, this.bodyYawO, this.bodyYaw);
    }

    // NBT

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("LockedYaw", this.lockedYaw);
        tag.putInt("PlantState", this.getState());
        if (this.anchorX != null) {
            tag.putDouble("AnchorX", this.anchorX);
            tag.putDouble("AnchorZ", this.anchorZ);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lockedYaw = tag.getFloat("LockedYaw");
        this.applyLockedYaw();
        this.setState(tag.getInt("PlantState"));
        if (tag.contains("AnchorX")) {
            this.anchorX = tag.getDouble("AnchorX");
            this.anchorZ = tag.getDouble("AnchorZ");
        }
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "base", 3, state -> switch (this.getState()) {
            case STATE_HIDDEN, STATE_HIDING -> state.setAndContinue(HIDE_ANIM);
            case STATE_REVEALING            -> state.setAndContinue(REVEAL_ANIM);
            default                         -> state.setAndContinue(IDLE_ANIM);
        }));
        registrar.add(new AnimationController<>(this, "attack_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
