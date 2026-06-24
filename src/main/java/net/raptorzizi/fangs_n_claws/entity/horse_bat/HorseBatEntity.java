package net.raptorzizi.fangs_n_claws.entity.horse_bat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
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

public class HorseBatEntity extends AbstractHorse implements GeoEntity, Enemy {

    private static final int FLIGHT_HEADROOM      = 3;
    private static final int MODE_SWITCH_COOLDOWN = 20;

    private static final int REAR_HIT_TICK    = 5;
    private static final int REAR_TOTAL_TICKS = 25;

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(HorseBatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FLAPPING =
            SynchedEntityData.defineId(HorseBatEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private final MoveControl    groundMoveControl;
    private final MoveControl    flyingMoveControl;

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;
    private int    modeCooldown        = 0;

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_SLOW_ANIM    = RawAnimation.begin().thenLoop("walk_slow");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM          = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation REAR_ANIM         = RawAnimation.begin().then("rear", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation EATING_GRASS_ANIM = RawAnimation.begin().thenLoop("eating_grass");
    private static final RawAnimation FLY_HOVERING_ANIM = RawAnimation.begin().thenLoop("fly_hovering");
    private static final RawAnimation FLY_FLAP_ANIM     = RawAnimation.begin().thenLoop("fly_flap");

    @SuppressWarnings("unchecked")
    public HorseBatEntity(EntityType<?> type, Level level) {
        super((EntityType<? extends AbstractHorse>) type, level);
        this.groundNavigation  = this.navigation;
        this.groundMoveControl = this.moveControl;
        FlyingPathNavigation flyNav = new FlyingPathNavigation(this, level);
        flyNav.setCanOpenDoors(false);
        flyNav.setCanFloat(true);
        flyNav.setCanPassDoors(false);
        this.flyingNavigation  = flyNav;
        this.flyingMoveControl = new HorseBatFlyMoveControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(IS_FLAPPING, false);
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Spawn

    public static AttributeSupplier.Builder prepareAttributes() {
        return createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.225)
                .add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (level instanceof ServerLevel serverLevel && serverLevel.random.nextInt(3) != 0) {
            int roll = serverLevel.random.nextInt(20);
            Mob rider = (roll < 11)
                    ? EntityType.SKELETON.create(serverLevel)
                    : (roll < 19)
                        ? EntityType.ZOMBIE.create(serverLevel)
                        : EntityRegistry.SILVER_SKELETON.get().create(serverLevel);
            if (rider != null) {
                rider.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                rider.finalizeSpawn(serverLevel, difficulty, MobSpawnType.MOB_SUMMONED, null);
                serverLevel.addFreshEntity(rider);
                rider.startRiding(this);
            }
        }
        return data;
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.setNoGravity(this.isFlying());

            if (this.isFlying()) {
                Vec3 m = this.getDeltaMovement();
                boolean moving = m.horizontalDistanceSqr() > 0.0025 || Math.abs(m.y) > 0.04;
                if (moving != this.isFlapping()) this.setFlapping(moving);
            } else if (this.isFlapping()) {
                this.setFlapping(false);
            }

            if (attackDelayTick > 0) {
                attackDelayTick++;
                if (attackDelayTick == REAR_HIT_TICK) {
                    if (pendingAttackTarget != null
                            && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= (this.getBbWidth() * 2.0F + pendingAttackTarget.getBbWidth() + 1.5F)) {
                        super.doHurtTarget(pendingAttackTarget);
                    }
                    pendingAttackTarget = null;
                }
                if (attackDelayTick >= REAR_TOTAL_TICKS) {
                    attackDelayTick = 0;
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0) return true;
        this.triggerAnim("attack_controller", "rear");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (modeCooldown > 0) modeCooldown--;
        LivingEntity target = this.getTarget();
        boolean wantFly = target != null && target.isAlive() && this.hasFlightHeadroom();
        if (wantFly != this.isFlying() && modeCooldown == 0) {
            this.setFlying(wantFly);
            modeCooldown = MODE_SWITCH_COOLDOWN;
        }

        if (this.level().isDay()) {
             float brightness = this.getLightLevelDependentMagicValue();
             BlockPos eyePos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
             boolean sheltered = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
             if (brightness > 0.5F && !sheltered && this.level().canSeeSky(eyePos)) {
                 this.igniteForSeconds(8);
             }
        }
    }

    // Fly

    public boolean isFlying()   { return this.entityData.get(IS_FLYING); }
    public boolean isFlapping() { return this.entityData.get(IS_FLAPPING); }
    private void setFlapping(boolean f) { this.entityData.set(IS_FLAPPING, f); }

    public void setFlying(boolean flying) {
        if (this.isFlying() == flying) return;
        this.entityData.set(IS_FLYING, flying);
        if (!this.level().isClientSide) {
            this.navigation.stop();
            this.navigation  = flying ? this.flyingNavigation  : this.groundNavigation;
            this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
            this.setNoGravity(flying);
        }
    }

    private boolean hasFlightHeadroom() {
        BlockPos head = this.blockPosition().above(Mth.ceil(this.getBbHeight()));
        for (int i = 0; i < FLIGHT_HEADROOM; i++) {
            if (this.level().getBlockState(head.above(i)).blocksMotion()) return false;
        }
        return true;
    }

    // AbstractHorse overrides

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && this.hasPassenger(attacker)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean isTamed() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
            new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("rear", REAR_ANIM)
        );

        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isFlying() || !this.onGround()) {
                if (this.isFlapping()) return state.setAndContinue(FLY_FLAP_ANIM);
                return state.setAndContinue(FLY_HOVERING_ANIM);
            }

            if (this.isEating()) return state.setAndContinue(EATING_GRASS_ANIM);

            float animSpeed = this.walkAnimation.speed();
            if (animSpeed > 0.55f) return state.setAndContinue(RUN_ANIM);
            if (animSpeed > 0.25f) return state.setAndContinue(WALK_ANIM);
            if (animSpeed > 0.05f) return state.setAndContinue(WALK_SLOW_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
