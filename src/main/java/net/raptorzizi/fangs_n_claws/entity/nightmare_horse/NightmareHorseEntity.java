package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
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

import java.util.HashSet;
import java.util.Set;

public class NightmareHorseEntity extends AbstractHorse implements GeoEntity, Enemy {

    public static final int VARIANT_RED   = 0;
    public static final int VARIANT_BLACK = 1;

    private static final int    REJECT_HIT_TICK    = 12;
    private static final int    REJECT_TOTAL_TICKS = 20;
    private static final double REJECT_KNOCKBACK   = 2.0;

    private static final int REAR_HIT_TICK    = 5;
    private static final int REAR_TOTAL_TICKS = 25;

    public static final int PHASE_IDLE   = 0;
    public static final int PHASE_WINDUP = 1;
    public static final int PHASE_CHARGE = 2;

    public  static final double CHARGE_SPEED      = 0.55;
    public  static final double BACKUP_SPEED      = 0.12;
    private static final int    CHARGE_COOLDOWN   = 120;
    private static final int    WINDUP_TICKS      = 20;
    private static final int    CHARGE_TICKS      = 25;
    private static final double CHARGE_KNOCKBACK  = 1.2;
    private static final int    CHARGE_FIRE_SECS  = 4;

    private static final float BOOST_MULT       = 1.5f;
    private static final float STAMINA_DRAIN    = 0.0125f;
    private static final float STAMINA_RECHARGE = 0.0062f;

    private static final EntityDataAccessor<Boolean> STURDY_SADDLED =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STAMINA =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_BOOSTING =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int    tamingBuckCounter = 0;
    private int    rejectTick        = 0;
    private Player tamingPlayer      = null;
    private Vec3   pendingKick       = null;

    private boolean boostHeld = false;

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    private int   chargePhase    = PHASE_IDLE;
    private Vec3  chargeDir      = Vec3.ZERO;
    private int   chargeCooldown = 0;
    private int   windupTimer    = 0;
    private int   chargeTimer    = 0;
    private final Set<Integer> chargeHits = new HashSet<>();

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_SLOW_ANIM    = RawAnimation.begin().thenLoop("walk_slow");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM          = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation REAR_ANIM         = RawAnimation.begin().then("rear", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation REJECT_ANIM       = RawAnimation.begin().then("reject", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation EATING_GRASS_ANIM = RawAnimation.begin().thenLoop("eating_grass");

    public NightmareHorseEntity(EntityType<?> type, Level level) {
        super((EntityType<? extends AbstractHorse>) type, level);
        this.moveControl = new NightmareHorseMoveControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STURDY_SADDLED, false);
        builder.define(VARIANT, VARIANT_RED);
        builder.define(STAMINA, 1.0F);
        builder.define(IS_BOOSTING, false);
    }

    public float getStamina()        { return this.entityData.get(STAMINA); }
    public void  setStamina(float v) { this.entityData.set(STAMINA, Mth.clamp(v, 0.0F, 1.0F)); }

    public boolean isBoosting()           { return this.entityData.get(IS_BOOSTING); }
    public void    setBoostHeld(boolean v) { this.boostHeld = v; }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SturdySaddled", this.isSaddled());
        tag.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSturdySaddled(tag.getBoolean("SturdySaddled"));
        this.setVariant(tag.getInt("Variant"));
    }

    public void setSturdySaddled(boolean v) { this.entityData.set(STURDY_SADDLED, v); }

    @Override
    public boolean isSaddled() { return this.entityData.get(STURDY_SADDLED); }

    public int  getVariant()      { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, v); }

    public boolean isStruggling() {
        return rejectTick == 0 && !this.isTamed() && this.getFirstPassenger() instanceof Player;
    }

    @Override
    public int getMaxTemper() {
        return 200;
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NightmareHorseStruggleGoal(this));
        this.goalSelector.addGoal(2, new NightmareHorseChargeGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Spawn

    public static AttributeSupplier.Builder prepareAttributes() {
        return createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.225)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(this.random.nextInt(2));
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (level instanceof ServerLevel serverLevel && shouldSpawnRider(serverLevel.random)) {
            Mob rider = createRider(serverLevel);
            if (rider != null) {
                rider.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                rider.finalizeSpawn(serverLevel, difficulty, MobSpawnType.MOB_SUMMONED, null);
                serverLevel.addFreshEntity(rider);
                rider.startRiding(this);
            }
        }
        return data;
    }

    protected boolean shouldSpawnRider(RandomSource random) {
        return true;
    }

    @Nullable
    protected Mob createRider(ServerLevel level) {
        return level.random.nextBoolean()
                ? EntityType.WITHER_SKELETON.create(level)
                : EntityType.SKELETON.create(level);
    }

    // Charge

    public int  getChargePhase()    { return chargePhase; }
    public Vec3 getChargeDir()      { return chargeDir; }
    public int  getChargeCooldown() { return chargeCooldown; }

    public void startWindup() {
        this.chargePhase = PHASE_WINDUP;
        this.windupTimer = WINDUP_TICKS;
        this.triggerAnim("attack_controller", "rear");
        this.playSound(SoundEvents.HORSE_ANGRY, 1.2F, 0.6F);
    }

    public void tickWindup(@Nullable LivingEntity target) {
        this.spawnHeadSmoke();
        if (--windupTimer <= 0) {
            Vec3 dir = (target != null)
                    ? new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ())
                    : this.getLookAngle();
            dir = new Vec3(dir.x, 0.0, dir.z);
            this.chargeDir = dir.lengthSqr() < 1.0E-4 ? this.getLookAngle().multiply(1, 0, 1).normalize() : dir.normalize();
            this.chargePhase = PHASE_CHARGE;
            this.chargeTimer = CHARGE_TICKS;
            this.chargeHits.clear();
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.2F);
        }
    }

    public void tickCharge() {
        this.spawnLegFire();
        this.applyChargeHits();
        if (--chargeTimer <= 0) {
            this.endCharge();
        }
    }

    public void endCharge() {
        if (this.chargePhase != PHASE_IDLE) {
            this.chargeCooldown = CHARGE_COOLDOWN;
        }
        this.chargePhase = PHASE_IDLE;
        this.windupTimer = 0;
        this.chargeTimer = 0;
        this.chargeHits.clear();
    }

    private void applyChargeHits() {
        AABB box = this.getBoundingBox().inflate(0.3);
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !this.hasPassenger(e))) {
            if (!chargeHits.add(victim.getId())) continue;
            victim.hurt(this.damageSources().mobAttack(this), dmg);
            victim.igniteForSeconds(CHARGE_FIRE_SECS);
            victim.knockback(CHARGE_KNOCKBACK, -chargeDir.x, -chargeDir.z);
            victim.hurtMarked = true;
            if (victim instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }

    private void applyBoostHits() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yaw), fz = Mth.cos(yaw);
        AABB box = this.getBoundingBox().inflate(0.2);
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !this.hasPassenger(e))) {
            victim.igniteForSeconds(CHARGE_FIRE_SECS);
            if (victim.invulnerableTime <= 0) {
                victim.hurt(this.damageSources().mobAttack(this), dmg);
                victim.knockback(CHARGE_KNOCKBACK, -fx, -fz);
                victim.hurtMarked = true;
                if (victim instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
                }
            }
        }
    }

    private void spawnHeadSmoke() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        Vec3 p = partPos(0.8, 1.5, 0.0);
        sl.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 4, 0.1, 0.1, 0.1, 0.01);
    }

    private void spawnLegFire() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        Vec3 l3 = partPos(0.7, 0.2,  0.35);
        Vec3 l4 = partPos(0.7, 0.2, -0.35);
        sl.sendParticles(ParticlesRegistry.FIRE.get(), l3.x, l3.y, l3.z, 2, 0.05, 0.05, 0.05, 0.01);
        sl.sendParticles(ParticlesRegistry.FIRE.get(), l4.x, l4.y, l4.z, 2, 0.05, 0.05, 0.05, 0.01);
    }

    private Vec3 partPos(double forward, double up, double side) {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yaw), fz = Mth.cos(yaw);
        double rx =  Mth.cos(yaw), rz = Mth.sin(yaw);
        return new Vec3(
                this.getX() + fx * forward + rx * side,
                this.getY() + up,
                this.getZ() + fz * forward + rz * side);
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (chargeCooldown > 0) chargeCooldown--;

            boolean hasRider = this.getControllingPassenger() instanceof Player;
            if (!hasRider) this.boostHeld = false;
            boolean boosting = this.boostHeld && hasRider && this.getStamina() > 0.0F;
            this.entityData.set(IS_BOOSTING, boosting);

            if (this.boostHeld && this.getStamina() > 0.0F) {
                this.setStamina(this.getStamina() - STAMINA_DRAIN);
            } else if (!this.boostHeld) {
                this.setStamina(this.getStamina() + STAMINA_RECHARGE);
            }

            if (boosting) {
                this.spawnLegFire();
                this.applyBoostHits();
            }

            if (tamingBuckCounter > 0) {
                if (this.isTamed() || tamingPlayer == null || tamingPlayer.getVehicle() != this) {
                    tamingBuckCounter = 0;
                    tamingPlayer = null;
                } else if (--tamingBuckCounter == 0) {
                    this.modifyTemper(5);
                    if (this.random.nextInt(this.getMaxTemper()) < this.getTemper()) {
                        this.tameWithName(tamingPlayer);
                        this.tamingPlayer = null;
                    } else {
                        this.triggerAnim("attack_controller", "reject");
                        this.rejectTick = 1;
                    }
                }
            }

            if (rejectTick > 0) {
                rejectTick++;

                if (rejectTick == REJECT_HIT_TICK && tamingPlayer != null && tamingPlayer.getVehicle() == this) {
                    Vec3 look = this.getLookAngle();
                    tamingPlayer.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    tamingPlayer.stopRiding();
                    this.pendingKick = new Vec3(-look.x, 0.0, -look.z).normalize().scale(REJECT_KNOCKBACK);
                    this.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }

                if (rejectTick == REJECT_HIT_TICK + 3 && tamingPlayer != null && pendingKick != null) {
                    tamingPlayer.setDeltaMovement(pendingKick.x, 0.5, pendingKick.z);
                    tamingPlayer.hasImpulse = true;
                    tamingPlayer.hurtMarked = true;
                    if (tamingPlayer instanceof ServerPlayer sp) {
                        sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
                    }
                    this.pendingKick = null;
                }

                if (rejectTick >= REJECT_TOTAL_TICKS) {
                    rejectTick = 0;
                    this.tamingPlayer = null;
                    this.pendingKick = null;
                }
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

    // AbstractHorse overrides

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled() && this.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    public void openCustomInventoryScreen(@NotNull Player player) {
    }

    @Override
    public boolean dismountsUnderwater() {
        return true;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level().isClientSide) {
            this.triggerAnim("attack_controller", "rear");
        }
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        float base = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        return this.isBoosting() ? base * BOOST_MULT : base;
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
    public boolean canAttack(@NotNull LivingEntity target) {
        if (this.isTamed()) return false;
        return !this.hasPassenger(target) && super.canAttack(target);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSecondaryUseActive() && this.isSaddled()) {
            if (!this.level().isClientSide) {
                this.setSturdySaddled(false);
                this.spawnAtLocation(new ItemStack(ItemsRegistry.STURDY_SADDLE.get()));
                this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isVehicle()) return InteractionResult.PASS;

        if (this.isTamed() && !this.isSaddled() && stack.is(ItemsRegistry.STURDY_SADDLE.get())) {
            if (!this.level().isClientSide) {
                this.setSturdySaddled(true);
                this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                this.doPlayerRide(player);
                if (!this.isTamed()) {
                    this.tamingPlayer = player;
                    this.tamingBuckCounter = 30 + this.random.nextInt(30);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.SKELETON_HORSE_HURT;
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
                new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                        .triggerableAnim("rear", REAR_ANIM)
                        .triggerableAnim("reject", REJECT_ANIM)
        );

        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isEating()) return state.setAndContinue(EATING_GRASS_ANIM);

            if (this.isJumping()) return state.setAndContinue(REAR_ANIM);

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
