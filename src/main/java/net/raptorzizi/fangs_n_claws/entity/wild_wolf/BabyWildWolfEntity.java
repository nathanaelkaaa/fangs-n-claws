package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BabyWildWolfEntity extends TamableAnimal implements GeoEntity {

    // Constants

    private static final int    TAME_CHANCE   = 3;
    private static final int    GROW_UP_TICKS = 24000;
    private static final float  HEAL_AMOUNT   = 4.0F;
    private static final double PACK_RADIUS   = 16.0;

    public static final TagKey<Item> WILD_WOLF_FOOD =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(
                    FangsClawsMod.MOD_ID, "wild_wolf_food"));

    // Synched data

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BabyWildWolfEntity.class, EntityDataSerializers.INT);

    // Animations

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT_ANIM  = RawAnimation.begin().thenLoop("sit");

    // Fields

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private UUID           parentUuid;
    private WildWolfEntity parentCache;
    private int            growthTick = 0;

    // Setup

    public BabyWildWolfEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           10.0)
                .add(Attributes.MOVEMENT_SPEED,        0.28)
                .add(Attributes.FOLLOW_RANGE,         20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, WildWolfEntity.VARIANT_BLACK);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
            @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(WildWolfEntity.variantForBiome(level, this.blockPosition()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    // Variant

    public int  getVariant()      { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, v); }

    // Render

    public String textureBaseName() {
        String prefix = switch (getVariant()) {
            case WildWolfEntity.VARIANT_BROWN     -> "brown";
            case WildWolfEntity.VARIANT_DARK_GRAY -> "dark_gray";
            case WildWolfEntity.VARIANT_GRAY      -> "gray";
            case WildWolfEntity.VARIANT_WHITE     -> "snowy";
            default                               -> "black";
        };
        return prefix + "_baby_wild_wolf";
    }

    public String geoName()       { return "baby_wild_wolf"; }
    public String animationName() { return "baby_wild_wolf"; }

    // Parent

    protected Class<? extends WildWolfEntity> adultClass() { return WildWolfEntity.class; }

    public void setParent(@Nullable WildWolfEntity parent) {
        this.parentCache = parent;
        this.parentUuid  = parent == null ? null : parent.getUUID();
    }

    @Nullable
    public WildWolfEntity getParent() {
        if (parentCache != null && parentCache.isAlive()) return parentCache;
        if (parentUuid != null && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(parentUuid) instanceof WildWolfEntity adult) {
                parentCache = adult;
                return adult;
            }
        }
        return null;
    }

    public boolean isInPack() {
        Class<? extends WildWolfEntity> adult = adultClass();
        return !this.level().getEntitiesOfClass(WildWolfEntity.class,
                this.getBoundingBox().inflate(PACK_RADIUS),
                w -> w.isAlive() && w.getClass() == adult).isEmpty();
    }

    // Taming

    public TagKey<Item> foodTag() { return WILD_WOLF_FOOD; }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return stack.is(foodTag());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob other) {
        return null;
    }

    // Interaction

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Level level = this.level();

        if (this.isTame()) {
            if (!this.isOwnedBy(player)) return super.mobInteract(player, hand);
            return interactTamed(player, hand, stack, level);
        }

        if (!isFood(stack)) return super.mobInteract(player, hand);
        return interactUntamed(player, stack, level);
    }

    private InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack, Level level) {
        if (isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.heal(HEAL_AMOUNT);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult interactUntamed(Player player, ItemStack stack, Level level) {
        if (this.isInPack()) {
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.getX(), this.getEyeY() + 0.5, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
        if (!level.isClientSide) {
            if (this.random.nextInt(TAME_CHANCE) == 0) {
                this.tame(player);
                this.setOrderedToSit(true);
                this.setParent(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.2, 8.0F, 3.0F));
        this.goalSelector.addGoal(4, new BabyPackFollowGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // Sound

    @Override protected SoundEvent getAmbientSound()                       { return SoundEvents.WOLF_WHINE; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource src) { return SoundEvents.WOLF_HURT; }
    @Override protected SoundEvent getDeathSound()                         { return SoundEvents.WOLF_DEATH; }
    @Override protected float getSoundVolume()                             { return 0.5F; }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (++this.growthTick >= GROW_UP_TICKS) growUp();
    }

    // Grow up

    protected EntityType<? extends WildWolfEntity> adultType() {
        return EntityRegistry.WILD_WOLF.get();
    }

    private void growUp() {
        if (!(this.level() instanceof ServerLevel server)) return;
        WildWolfEntity adult = adultType().create(server);
        if (adult == null) return;

        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(adult.blockPosition()),
                MobSpawnType.CONVERSION, null);
        adult.setVariant(this.getVariant());
        if (this.isTame()) adult.setOwnerUUID(this.getOwnerUUID());
        if (this.hasCustomName()) {
            adult.setCustomName(this.getCustomName());
            adult.setCustomNameVisible(this.isCustomNameVisible());
        }
        server.addFreshEntity(adult);
        this.discard();
    }

    // Save data

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putInt("Growth", growthTick);
        if (parentUuid != null) tag.putUUID("ParentUUID", parentUuid);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        growthTick = tag.getInt("Growth");
        if (tag.hasUUID("ParentUUID")) parentUuid = tag.getUUID("ParentUUID");
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving())       return state.setAndContinue(WALK_ANIM);
            if (this.isInSittingPose()) return state.setAndContinue(SIT_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
