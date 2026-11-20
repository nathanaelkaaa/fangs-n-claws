package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import java.util.OptionalInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EntEntity extends PathfinderMob implements GeoEntity, InventoryCarrier {

    // Guard
    public static final double GUARD_RADIUS = 20.0;
    private static final float LOG_WAKE_CHANCE = 0.20F;
    private static final double FOLLOW_RANGE = 24.0;

    // Sleep
    private static final int WAKE_UP_TICKS = 40;
    private static final int SELF_WAKE_ODDS = 12000;
    private static final int AWAKE_MIN_TICKS = 1200;
    private static final int AWAKE_EXTRA_TICKS = 1200;
    private static final int SPAWN_SEPARATION = 48;

    // Combat
    private static final int ATTACK_HIT_TICK = 6;
    private static final int ATTACK_TOTAL_TICKS = 15;
    private static final int DIG_HIT_TICK = 8;
    private static final int DIG_TOTAL_TICKS = 15;
    private static final double STRIKE_REACH = 4.0;
    private static final float KNOCK_UP = 0.4F;
    private static final int DIG_SLOW_TICKS = 30;
    private static final int DIG_SLOW_LEVEL = 3;
    private static final float FIRE_MULTIPLIER = 2.0F;
    private static final float AXE_MULTIPLIER = 1.5F;

    // Inventory
    private static final int INVENTORY_SIZE = 8;

    // Blessing
    private static final int BLESSING_TICKS = 12000;

    private static final ResourceLocation OAK_TEXTURE = FangsClawsMod.id("textures/entity/ent_oak.png");

    private static final EntityDataAccessor<Boolean> SLEEPING =
            SynchedEntityData.defineId(EntEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAKING =
            SynchedEntityData.defineId(EntEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation WAKE_UP = RawAnimation.begin().then("wake_up", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_DIG = RawAnimation.begin().then("attack_dig", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation PLANT = RawAnimation.begin().then("plant", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    private int wakeUpTick;
    private int awakeTicks;
    private int awakeLimit = AWAKE_MIN_TICKS;
    private int selfPlantCooldown;

    private Entity pendingTarget;
    private int attackTick;
    private int digTick;

    public EntEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    public static <T extends EntEntity> boolean checkEntSpawnRules(EntityType<T> type, ServerLevelAccessor level,
                                             MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.below()).is(BlockTags.DIRT)) return false;
        if (!clearExceptLeaves(type, level, pos)) return false;
        if (!Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) return false;
        if (!(level instanceof ServerLevel serverLevel)) return true;
        AABB area = new AABB(pos).inflate(SPAWN_SEPARATION, 16, SPAWN_SEPARATION);
        return serverLevel.getEntitiesOfClass(EntEntity.class, area).isEmpty();
    }

    private static boolean clearExceptLeaves(EntityType<? extends EntEntity> type, ServerLevelAccessor level, BlockPos pos) {
        double halfWidth = type.getWidth() / 2.0;
        AABB body = new AABB(pos.getX() + 0.5 - halfWidth, pos.getY(), pos.getZ() + 0.5 - halfWidth,
                pos.getX() + 0.5 + halfWidth, pos.getY() + type.getHeight(), pos.getZ() + 0.5 + halfWidth);

        for (BlockPos cell : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY, body.minZ),
                BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
            BlockState state = level.getBlockState(cell);
            if (!state.getFluidState().isEmpty()) return false;
            if (state.is(BlockTags.LEAVES)) continue;
            if (!state.getCollisionShape(level, cell).isEmpty()) return false;
        }
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(WAKING, false);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                                  @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        this.fallAsleep();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EntSleepGoal(this));
        this.goalSelector.addGoal(2, new EntAttackGoal(this));
        this.goalSelector.addGoal(3, new EntPlantGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // Sleep

    // Essence

    public ResourceLocation textureLocation() {
        return OAK_TEXTURE;
    }

    /**
     * Vide pour un feuillage teinte par le biome, comme les feuilles de chene. Une essence dont les
     * feuilles vanilla ont une teinte fixe, comme le bouleau, la redonne ici.
     */
    public OptionalInt fixedFoliageColor() {
        return OptionalInt.empty();
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public boolean isWakingUp() {
        return this.entityData.get(WAKING);
    }

    public boolean isDormant() {
        return this.isSleeping() || this.isWakingUp();
    }

    public void fallAsleep() {
        this.entityData.set(SLEEPING, true);
        this.entityData.set(WAKING, false);
        this.getNavigation().stop();
        this.awakeTicks = 0;
    }

    public void startWakingUp() {
        if (!this.isSleeping()) return;
        this.entityData.set(SLEEPING, false);
        this.entityData.set(WAKING, true);
        this.wakeUpTick = 1;
        this.awakeTicks = 0;
        this.awakeLimit = AWAKE_MIN_TICKS + this.random.nextInt(AWAKE_EXTRA_TICKS);
        this.triggerAnim("events", "wake_up");
        this.playSound(SoundsRegistry.ENT_WAKE_UP.get(), 1.2F, 1.0F);
    }

    // Guard

    public void onLogBroken(Player culprit) {
        if (this.isSleeping()) {
            if (this.random.nextFloat() >= LOG_WAKE_CHANCE) return;
            this.startWakingUp();
        }
        this.setTarget(culprit);
    }

    private static boolean canBeAngryAt(LivingEntity entity) {
        return !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private void forgetLostTarget() {
        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (!target.isAlive() || !canBeAngryAt(target) || this.distanceToSqr(target) > FOLLOW_RANGE * FOLLOW_RANGE) {
            this.setTarget(null);
        }
    }

    // Planting

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public static boolean isPlantable(ItemStack stack) {
        return stack.is(ItemTags.SAPLINGS) || stack.is(ItemTags.SMALL_FLOWERS) || stack.is(ItemTags.TALL_FLOWERS)
                || stack.is(Items.RED_MUSHROOM) || stack.is(Items.BROWN_MUSHROOM);
    }

    public boolean hasPlantables() {
        return this.inventory.hasAnyMatching(EntEntity::isPlantable);
    }

    public boolean canSelfPlant() {
        return this.selfPlantCooldown <= 0;
    }

    public void onSelfPlanted() {
        this.selfPlantCooldown = 2400 + this.random.nextInt(3600);
    }

    public void keepAwake() {
        this.awakeTicks = 0;
    }

    public void playPlantAnimation() {
        this.triggerAnim("events", "plant");
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isDormant() && super.canPickUpLoot();
    }

    @Override
    public boolean wantsToPickUp(@NotNull ItemStack stack) {
        return isPlantable(stack) && this.inventory.canAddItem(stack);
    }

    @Override
    protected void pickUpItem(@NotNull ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        boolean sapling = stack.is(ItemTags.SAPLINGS);
        int before = stack.getCount();
        Entity thrower = itemEntity.getOwner();

        InventoryCarrier.pickUpItem(this, this, itemEntity);

        boolean taken = itemEntity.isRemoved() || itemEntity.getItem().getCount() < before;
        if (sapling && taken && thrower instanceof Player player) this.bless(player);
    }

    private void bless(Player player) {
        player.addEffect(new MobEffectInstance(MobEffectsRegistry.FOREST_BLESSING, BLESSING_TICKS), this);
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!isPlantable(held) || this.getTarget() == player) return super.mobInteract(player, hand);
        if (this.level().isClientSide) return InteractionResult.SUCCESS;

        boolean sapling = held.is(ItemTags.SAPLINGS);
        ItemStack rest = this.inventory.addItem(held.copy());
        int accepted = held.getCount() - rest.getCount();
        if (accepted <= 0) return InteractionResult.FAIL;
        if (!player.getAbilities().instabuild) held.shrink(accepted);
        if (sapling) this.bless(player);

        this.startWakingUp();
        this.keepAwake();
        this.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 0.6F);
        ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(), 8, 0.6, 0.6, 0.6, 0.0);
        return InteractionResult.CONSUME;
    }

    // Combat

    public boolean isAttacking() {
        return this.attackTick > 0 || this.digTick > 0;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        this.triggerAnim("events", "attack");
        this.pendingTarget = target;
        this.attackTick = 1;
        return true;
    }

    public void doDigAttack(Entity target) {
        this.triggerAnim("events", "attack_dig");
        this.pendingTarget = target;
        this.digTick = 1;
    }

    private boolean strike(Entity target) {
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = (int) base > 0 ? base / 2.0F + this.random.nextInt((int) base) : base;
        DamageSource source = this.damageSources().mobAttack(this);
        boolean hit = target.hurt(source, damage);
        if (hit) {
            double resistance = target instanceof LivingEntity living
                    ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 0.0;
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, KNOCK_UP * Math.max(0.0, 1.0 - resistance), 0.0));
            EnchantmentHelper.doPostAttackEffects((ServerLevel) this.level(), target, source);
        }
        this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 0.6F);
        return hit;
    }

    private void dig(Entity target) {
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource source = this.damageSources().mobAttack(this);
        if (!target.hurt(source, base)) return;

        BlockPos below = target.blockPosition().below();
        BlockState ground = this.level().getBlockState(below);
        if (target.onGround() && ground.isSolidRender(this.level(), below)) {
            target.setDeltaMovement(0.0, 0.0, 0.0);
            target.teleportTo(target.getX(), target.getY() - 1.0, target.getZ());
            target.hurtMarked = true;
        }
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DIG_SLOW_TICKS, DIG_SLOW_LEVEL), this);
        }
        ((ServerLevel) this.level()).sendParticles(ParticleTypes.CLOUD,
                target.getX(), target.getY(), target.getZ(), 10, 0.4, 0.1, 0.4, 0.02);
        this.playSound(SoundsRegistry.ENT_ATTACK_DIG.get(), 1.4F, 1.0F);
    }

    private boolean inReach(Entity target) {
        return target != null && target.isAlive()
                && this.distanceTo(target) <= STRIKE_REACH && this.hasLineOfSight(target);
    }
    
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) amount *= FIRE_MULTIPLIER;
        if (source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().getItem() instanceof AxeItem) {
            amount *= AXE_MULTIPLIER;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker
                && attacker != this && canBeAngryAt(attacker)) {
            this.startWakingUp();
            this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (!this.isDormant()) super.knockback(strength, x, z);
    }

    @Override
    public boolean isPushable() {
        return !this.isDormant() && super.isPushable();
    }

    // Tick

    @Override
    public void tick() {
        super.tick();

        if (this.isDormant()) {
            float snapped = Math.round(this.getYRot() / 90.0F) * 90.0F;
            this.setYRot(snapped);
            this.yRotO = snapped;
            this.yHeadRot = snapped;
            this.yHeadRotO = snapped;
            this.yBodyRot = snapped;
            this.yBodyRotO = snapped;
        }

        if (this.level().isClientSide) return;

        if (this.selfPlantCooldown > 0) this.selfPlantCooldown--;
        this.forgetLostTarget();
        this.tickAttacks();

        if (this.isSleeping()) {
            if (this.random.nextInt(SELF_WAKE_ODDS) == 0) {
                this.selfPlantCooldown = 0;
                this.startWakingUp();
            }
            return;
        }

        if (this.wakeUpTick > 0) {
            this.wakeUpTick++;
            this.getNavigation().stop();
            if (this.wakeUpTick >= WAKE_UP_TICKS) {
                this.wakeUpTick = 0;
                this.entityData.set(WAKING, false);
            }
            return;
        }

        boolean busy = this.getTarget() != null || this.hasPlantables() || this.isAttacking();
        if (busy) {
            this.awakeTicks = 0;
        } else if (++this.awakeTicks >= this.awakeLimit) {
            this.fallAsleep();
        }
    }

    private void tickAttacks() {
        if (this.attackTick > 0) {
            this.attackTick++;
            if (this.attackTick == ATTACK_HIT_TICK && this.inReach(this.pendingTarget)) this.strike(this.pendingTarget);
            if (this.attackTick >= ATTACK_TOTAL_TICKS) this.attackTick = 0;
        }
        if (this.digTick > 0) {
            this.digTick++;
            if (this.digTick == DIG_HIT_TICK && this.inReach(this.pendingTarget)) this.dig(this.pendingTarget);
            if (this.digTick >= DIG_TOTAL_TICKS) this.digTick = 0;
        }
        if (!this.isAttacking()) this.pendingTarget = null;
    }

    // Persistence

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putInt("SelfPlantCooldown", this.selfPlantCooldown);
        this.writeInventoryToTag(tag, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Sleeping")) this.fallAsleep();
        this.selfPlantCooldown = tag.getInt("SelfPlantCooldown");
        this.readInventoryFromTag(tag, this.registryAccess());
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
    }

    // Sound

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isDormant() ? null : SoundsRegistry.ENT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundsRegistry.ENT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsRegistry.ENT_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.WOOD_STEP, 1.0F, 0.6F);
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isSleeping()) return state.setAndContinue(SLEEP);
            if (this.isWakingUp()) return PlayState.STOP;
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
        registrar.add(new AnimationController<>(this, "events", 2, state -> PlayState.STOP)
                .triggerableAnim("wake_up", WAKE_UP)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("attack_dig", ATTACK_DIG)
                .triggerableAnim("plant", PLANT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
