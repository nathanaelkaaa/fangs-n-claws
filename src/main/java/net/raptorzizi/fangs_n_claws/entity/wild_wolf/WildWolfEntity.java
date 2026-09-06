package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterFollowOwnerGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterOwnerHurtByTargetGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterOwnerHurtTargetGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterSitGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.TamableCreature;
import net.raptorzizi.fangs_n_claws.entity.tame.TamedRules;
import net.raptorzizi.fangs_n_claws.entity.tame.TamedData;
import net.raptorzizi.fangs_n_claws.item.armor.FurArmorItem;
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
import java.util.Optional;
import java.util.UUID;

public class WildWolfEntity extends Monster implements GeoEntity, TamableCreature {

    // Constants

    private static final int    ATTACK_HIT_TICK    = 7;
    private static final int    ATTACK_TOTAL_TICKS = 15;
    private static final double ATTACK_RANGE       = 2.0;

    private static final double PACK_ALERT_RADIUS  = 16.0;
    private static final int    PACK_ALERT_PERIOD  = 20;
    private static final int    HOWL_COOLDOWN      = 300;

    private static final float  VOICE_PITCH        = 0.9F;

    private static final double LEADER_RADIUS         = 16.0;
    private static final int    LEADER_RESOLVE_PERIOD = 20;
    private static final int    PANIC_DURATION        = 80;

    private static final float  HEAL_AMOUNT = 10.0F;
    private static final int    LOVE_DURATION   = 600;
    private static final int    BREED_COOLDOWN  = 6000;
    private static final int    LOVE_HEART_PERIOD = 10;

    public static final int VARIANT_BLACK     = 0;
    public static final int VARIANT_BROWN     = 1;
    public static final int VARIANT_DARK_GRAY = 2;
    public static final int VARIANT_GRAY      = 3;
    public static final int VARIANT_WHITE     = 4;

    // Synched data

    private static final EntityDataAccessor<Integer> VARIANT   = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LEADER = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> TAMED          = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ORDERED_TO_SIT = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COLLAR_COLOR   = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER   = SynchedEntityData.defineId(WildWolfEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Animations

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT_ANIM    = RawAnimation.begin().thenLoop("sit");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);

    // Fields

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity         pendingAttackTarget = null;
    private int            attackDelayTick     = 0;
    private WildWolfEntity packLeader          = null;
    private int            inLoveTicks         = 0;
    private int            breedCooldown       = 0;
    private int            panicCooldown       = 0;
    private int            howlCooldown        = 0;

    // Setup

    public WildWolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           50.0)
                .add(Attributes.MOVEMENT_SPEED,        0.32)
                .add(Attributes.ATTACK_DAMAGE,         4.0)
                .add(Attributes.FOLLOW_RANGE,         20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT,        VARIANT_BLACK);
        builder.define(IS_LEADER,      false);
        builder.define(TAMED,          false);
        builder.define(ORDERED_TO_SIT, false);
        builder.define(OWNER,          Optional.empty());
        builder.define(COLLAR_COLOR,   DyeColor.RED.getId());
    }

    // Spawn

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(variantForBiome(level, this.blockPosition()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor level, @NotNull MobSpawnType spawnType) {
        if (level instanceof ServerLevelAccessor sla && sla.getDifficulty() == Difficulty.PEACEFUL) return false;
        BlockPos pos = this.blockPosition();
        if (level.getBrightness(LightLayer.SKY, pos) <= 0) return false;
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    public static int variantForBiome(ServerLevelAccessor level, BlockPos pos) {
        String p = level.getBiome(pos).unwrapKey().map(k -> k.location().getPath()).orElse("");

        if (p.contains("snowy") || p.contains("frozen") || p.contains("ice")
                || p.equals("grove") || p.contains("glacial")) return VARIANT_WHITE;
        if (p.equals("dark_forest")) return VARIANT_DARK_GRAY;
        if (p.equals("old_growth_pine_taiga") || p.equals("old_growth_spruce_taiga")) return VARIANT_BLACK;
        if (p.equals("taiga") || p.contains("birch")
                || p.contains("windswept") || p.contains("hills")
                || p.contains("peaks") || p.equals("meadow")) return VARIANT_GRAY;
        return VARIANT_BROWN;
    }

    // Variant

    public int  getVariant()      { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, v); }

    // Taming

    public TagKey<Item> foodTag() { return BabyWildWolfEntity.WILD_WOLF_FOOD; }

    public boolean isTamed()                { return this.entityData.get(TAMED); }
    public boolean isOwnedBy(Player player) { return player.getUUID().equals(getOwnerUUID()); }

    @Override
    @Nullable
    public UUID getOwnerUUID() { return this.entityData.get(OWNER).orElse(null); }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
        this.entityData.set(TAMED, uuid != null);
    }

    @Override
    public boolean isOrderedToSit() { return this.entityData.get(ORDERED_TO_SIT); }

    @Override
    public void setOrderedToSit(boolean sit) { this.entityData.set(ORDERED_TO_SIT, sit); }

    public DyeColor getCollarColor() { return DyeColor.byId(this.entityData.get(COLLAR_COLOR)); }

    public void setCollarColor(DyeColor colour) { this.entityData.set(COLLAR_COLOR, colour.getId()); }

    @Override
    public boolean isPreventingPlayerRest(@NotNull Player player) {
        return TamedRules.preventsPlayerRest(this) && super.isPreventingPlayerRest(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return TamedRules.allowsDespawn(this) && super.removeWhenFarAway(distance);
    }

    // Breeding

    public EntityType<? extends BabyWildWolfEntity> babyType() { return EntityRegistry.BABY_WILD_WOLF.get(); }

    public boolean isInLove()  { return inLoveTicks > 0; }
    public void    clearLove() { this.inLoveTicks = 0; }

    public boolean canBreed() { return isTamed() && breedCooldown <= 0 && !isInLove(); }

    public void setInLove() {
        this.inLoveTicks = LOVE_DURATION;
        this.breedCooldown = 0;
    }

    public void setBreedCooldown() { this.breedCooldown = BREED_COOLDOWN; }

    private void tickLove() {
        if (breedCooldown > 0) breedCooldown--;
        if (inLoveTicks <= 0) return;

        inLoveTicks--;
        if (inLoveTicks % LOVE_HEART_PERIOD == 0 && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART,
                    this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(),
                    1, this.getBbWidth() * 0.5, 0.3, this.getBbWidth() * 0.5, 0.02);
        }
    }

    // Interaction

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTamed() || !isOwnedBy(player)) return super.mobInteract(player, hand);

        if (stack.is(foodTag()) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.heal(HEAL_AMOUNT);
            spawnHealParticles();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(foodTag()) && canBreed()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!this.level().isClientSide) setInLove();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.getItem() instanceof DyeItem dye && dye.getDyeColor() != getCollarColor()) {
            setCollarColor(dye.getDyeColor());
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide) {
            setOrderedToSit(!isOrderedToSit());
            this.jumping = false;
            this.getNavigation().stop();
            this.setTarget(null);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MonsterSitGoal<>(this));
        this.goalSelector.addGoal(2, new WildWolfPanicGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(5, new WildWolfBreedGoal(this));
        this.goalSelector.addGoal(6, new MonsterFollowOwnerGoal<>(this, 1.2, 5.0F, 2.0F, 20.0F));
        this.goalSelector.addGoal(7, new WildWolfPackFollowGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new MonsterOwnerHurtByTargetGoal<>(this));
        this.targetSelector.addGoal(1, new MonsterOwnerHurtTargetGoal<>(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            protected void alertOther(@NotNull Mob mob, @NotNull LivingEntity target) {
                if (mob instanceof WildWolfEntity other && !WildWolfEntity.this.isPackMate(other)) return;
                super.alertOther(mob, target);
            }
        }.setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override protected double getFollowDistance() { return 8.0; }
            @Override public boolean canUse()              { return !isTamed() && super.canUse(); }
            @Override public boolean canContinueToUse()    { return !isTamed() && super.canContinueToUse(); }
        });
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Sheep.class, false) {
            @Override public boolean canUse()              { return !isTamed() && super.canUse(); }
            @Override public boolean canContinueToUse()    { return !isTamed() && super.canContinueToUse(); }
        });
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, false) {
            @Override public boolean canUse()              { return !isTamed() && super.canUse(); }
            @Override public boolean canContinueToUse()    { return !isTamed() && super.canContinueToUse(); }
        });
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Skeleton.class, false));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, SilverSkeletonEntity.class, false));
    }

    // Sound

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.WILD_WOLF_GROWL.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.WILD_WOLF_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.WILD_WOLF_DEATH.get(); }

    protected SoundEvent getAttackSound() { return SoundsRegistry.WILD_WOLF_GROWL.get(); }

    protected float basePitch() { return VOICE_PITCH; }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + this.basePitch();
    }

    @Nullable
    protected SoundEvent getPackHowlSound() { return SoundsRegistry.WILD_WOLF_HOWL.get(); }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    // Combat

    public boolean isAttacking() { return attackDelayTick > 0; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0) return true;
        this.triggerAnim("attack_controller", "attack");
        this.playSound(this.getAttackSound(), 1.0F, this.getVoicePitch());
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) tickAttack();
    }

    private void tickAttack() {
        if (attackDelayTick <= 0) return;

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

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.getTarget() instanceof Player p && (FurArmorItem.hasFurHelmet(p) || this.isOwnedBy(p))) {
            this.setTarget(null);
        }

        tickPack();
        tickLove();
    }

    // Pack

    public boolean isPackMate(WildWolfEntity other) {
        if (this.isTamed() || other.isTamed()) return false;
        return other.getType() == this.getType();
    }

    public boolean isPanicking() { return panicCooldown > 0; }

    @Nullable
    public WildWolfEntity getPackLeader() { return this.packLeader; }

    public boolean isLeader() { return this.entityData.get(IS_LEADER); }

    private void setLeaderFlag(boolean v) { this.entityData.set(IS_LEADER, v); }

    private void tickPack() {
        if (howlCooldown > 0) howlCooldown--;

        if (isTamed()) {
            if (packLeader != null || isLeader()) {
                packLeader = null;
                setLeaderFlag(false);
            }
            panicCooldown = 0;
        } else if (panicCooldown > 0) {
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
            boolean alerted = false;
            for (WildWolfEntity other : this.level().getEntitiesOfClass(WildWolfEntity.class,
                    this.getBoundingBox().inflate(PACK_ALERT_RADIUS),
                    w -> w != this && w.isAlive() && w.getTarget() == null && isPackMate(w))) {
                other.setTarget(target);
                alerted = true;
            }
            if (alerted && isLeader() && howlCooldown <= 0) {
                SoundEvent howl = this.getPackHowlSound();
                if (howl != null) {
                    this.playSound(howl, 3.0F, 0.95F + this.random.nextFloat() * 0.1F);
                    howlCooldown = HOWL_COOLDOWN;
                }
            }
        }
    }

    private void resolvePackLeader() {
        List<WildWolfEntity> neighbors = this.level().getEntitiesOfClass(WildWolfEntity.class,
                this.getBoundingBox().inflate(LEADER_RADIUS), w -> w != this && w.isAlive() && isPackMate(w));

        WildWolfEntity best = this;
        for (WildWolfEntity w : neighbors) {
            if (w.getUUID().compareTo(best.getUUID()) < 0) best = w;
        }
        this.packLeader = (best == this) ? null : best;
        boolean leader = (best == this) && !neighbors.isEmpty();
        if (this.isLeader() != leader) this.setLeaderFlag(leader);
    }

    // Particles

    private void spawnHealParticles() {
        if (!(this.level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.HEART,
                this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(),
                6, this.getBbWidth() * 0.5, 0.3, this.getBbWidth() * 0.5, 0.02);
    }

    // Save data

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant());
        TamedData.save(tag, this);
        tag.putByte("CollarColor", (byte) getCollarColor().getId());
        tag.putInt("InLove", inLoveTicks);
        tag.putInt("BreedCooldown", breedCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(tag.getInt("Variant"));
        TamedData.load(tag, this);
        if (tag.contains("CollarColor")) setCollarColor(DyeColor.byId(tag.getByte("CollarColor")));
        inLoveTicks   = tag.getInt("InLove");
        breedCooldown = tag.getInt("BreedCooldown");
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving())      return state.setAndContinue(WALK_ANIM);
            if (this.isOrderedToSit()) return state.setAndContinue(SIT_ANIM);
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
