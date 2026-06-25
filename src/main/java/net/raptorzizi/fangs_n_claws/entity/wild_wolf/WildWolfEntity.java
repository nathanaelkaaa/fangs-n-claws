package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
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

import java.util.List;

public class WildWolfEntity extends Monster implements GeoEntity {

    private static final int    ATTACK_HIT_TICK    = 7;
    private static final int    ATTACK_TOTAL_TICKS = 15;
    private static final double ATTACK_RANGE       = 2.0;

    private static final double PACK_ALERT_RADIUS  = 16.0;
    private static final int    PACK_ALERT_PERIOD  = 20;

    private static final double LEADER_RADIUS         = 16.0;
    private static final int    LEADER_RESOLVE_PERIOD = 20;
    private static final int    PANIC_DURATION        = 80;

    private static final EntityDataAccessor<Boolean> IS_LEADER =
            SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.BOOLEAN);

    private WildWolfEntity packLeader    = null;
    private int            panicCooldown = 0;

    public static final int VARIANT_BLACK     = 0;
    public static final int VARIANT_BROWN     = 1;
    public static final int VARIANT_DARK_GRAY = 2;
    public static final int VARIANT_GRAY      = 3;
    public static final int VARIANT_WHITE     = 4;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);

    public WildWolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Variant

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_BLACK);
        builder.define(IS_LEADER, false);
    }

    public int  getVariant()        { return this.entityData.get(VARIANT); }
    public void setVariant(int v)   { this.entityData.set(VARIANT, v); }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(tag.getInt("Variant"));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(variantForBiome(level, this.blockPosition()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    private static int variantForBiome(ServerLevelAccessor level, BlockPos pos) {
        String p = level.getBiome(pos).unwrapKey().map(k -> k.location().getPath()).orElse("");

        if (p.contains("snowy") || p.contains("frozen") || p.contains("ice")
                || p.equals("grove") || p.contains("glacial")) return VARIANT_WHITE;
        if (p.equals("dark_forest") || p.equals("old_growth_pine_taiga")
                || p.equals("old_growth_spruce_taiga")) return VARIANT_DARK_GRAY;
        if (p.contains("forest")) return VARIANT_BROWN;
        if (p.equals("meadow") || p.contains("hills") || p.contains("peaks") || p.contains("windswept")) return VARIANT_GRAY;
        return VARIANT_BLACK;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               14.0)
                .add(Attributes.MOVEMENT_SPEED,            0.32)
                .add(Attributes.ATTACK_DAMAGE,             4.0)
                .add(Attributes.FOLLOW_RANGE,             20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,      0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WildWolfPanicGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new WildWolfPackFollowGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            protected double getFollowDistance() {return 8.0;}});
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Sheep.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Skeleton.class, false));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, SilverSkeletonEntity.class, false));
    }

    // Sounds

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.WOLF_GROWL; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.WOLF_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.WOLF_DEATH; }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    // Combat

    public boolean isAttacking() { return attackDelayTick > 0; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0) return true;
        this.triggerAnim("attack_controller", "attack");
        this.playSound(SoundEvents.WOLF_GROWL, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && attackDelayTick > 0) {
            attackDelayTick++;
            if (attackDelayTick == ATTACK_HIT_TICK) {
                if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                        && this.distanceTo(pendingAttackTarget) <= ATTACK_RANGE + 1.0
                        && this.hasLineOfSight(pendingAttackTarget)) {
                    super.doHurtTarget(pendingAttackTarget);
                }
                pendingAttackTarget = null;
            }
            if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
        }
    }

    // Pack
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (panicCooldown > 0) {
            panicCooldown--;
        } else if (packLeader != null && (!packLeader.isAlive() || packLeader.isRemoved())) {
            panicCooldown = PANIC_DURATION;
            packLeader = null;
            setLeaderFlag(false);
        } else if (this.tickCount % LEADER_RESOLVE_PERIOD == 0) {
            resolvePackLeader();
        }

        LivingEntity target = this.getTarget();
        if (!isPanicking() && target != null && target.isAlive() && this.tickCount % PACK_ALERT_PERIOD == 0) {
            for (WildWolfEntity other : this.level().getEntitiesOfClass(WildWolfEntity.class,
                    this.getBoundingBox().inflate(PACK_ALERT_RADIUS),
                    w -> w != this && w.isAlive() && w.getTarget() == null)) {
                other.setTarget(target);
            }
        }
    }

    public boolean isPanicking() { return panicCooldown > 0; }

    private void resolvePackLeader() {
        List<WildWolfEntity> neighbors = this.level().getEntitiesOfClass(WildWolfEntity.class,
                this.getBoundingBox().inflate(LEADER_RADIUS), w -> w != this && w.isAlive());

        WildWolfEntity best = this;
        for (WildWolfEntity w : neighbors) {
            if (w.getUUID().compareTo(best.getUUID()) < 0) best = w;
        }
        this.packLeader = (best == this) ? null : best;
        boolean leader = (best == this) && !neighbors.isEmpty();
        if (this.isLeader() != leader) this.setLeaderFlag(leader);
    }

    @Nullable
    public WildWolfEntity getPackLeader() { return this.packLeader; }

    public boolean isLeader()             { return this.entityData.get(IS_LEADER); }
    private void   setLeaderFlag(boolean v) { this.entityData.set(IS_LEADER, v); }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
