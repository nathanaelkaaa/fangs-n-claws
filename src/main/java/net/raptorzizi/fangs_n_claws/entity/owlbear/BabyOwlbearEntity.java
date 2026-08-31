package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BabyOwlbearEntity extends TamableAnimal implements GeoEntity {

    // Constants

    private static final int   TAME_CHANCE   = 3;
    private static final int   GROW_UP_TICKS = 24000;
    private static final float HEAL_AMOUNT   = 4.0F;

    // Synched data

    private static final EntityDataAccessor<Boolean> WILD_SLEEPING =
            SynchedEntityData.defineId(BabyOwlbearEntity.class, EntityDataSerializers.BOOLEAN);

    // Animations

    private static final RawAnimation IDLE_ANIM  = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM  = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SLEEP_ANIM = RawAnimation.begin().thenLoop("sleep");

    // Fields

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private UUID          parentUuid;
    private OwlbearEntity parentCache;
    private int           growthTick = 0;

    // Setup

    public BabyOwlbearEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           12.0)
                .add(Attributes.MOVEMENT_SPEED,        0.25)
                .add(Attributes.ATTACK_DAMAGE,         1.0)
                .add(Attributes.FOLLOW_RANGE,         24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WILD_SLEEPING, false);
    }

    // State

    public boolean isWildSleeping()                  { return this.entityData.get(WILD_SLEEPING); }
    public void    setWildSleeping(boolean sleeping) { this.entityData.set(WILD_SLEEPING, sleeping); }

    public boolean isSleepPose() {
        return this.isInSittingPose() || this.isWildSleeping();
    }

    // Render

    public String textureBaseName() { return "baby_owlbear"; }

    // Parent

    public void setParent(@Nullable OwlbearEntity parent) {
        this.parentCache = parent;
        this.parentUuid  = parent == null ? null : parent.getUUID();
    }

    @Nullable
    public OwlbearEntity getParent() {
        if (parentCache != null && parentCache.isAlive()) return parentCache;
        if (parentUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(parentUuid);
            if (e instanceof OwlbearEntity owlbear) {
                parentCache = owlbear;
                return owlbear;
            }
        }
        return null;
    }

    // Taming

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SALMON);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    // Interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Level level = this.level();

        if (this.isTame()) {
            if (!this.isOwnedBy(player)) return super.mobInteract(player, hand);
            return interactTamed(player, stack, level);
        }

        if (!isFood(stack)) return super.mobInteract(player, hand);
        return interactUntamed(player, stack, level);
    }

    private InteractionResult interactTamed(Player player, ItemStack stack, Level level) {
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
        if (!player.getAbilities().instabuild) stack.shrink(1);
        if (!level.isClientSide) {
            if (this.random.nextInt(TAME_CHANCE) == 0) {
                this.tame(player);
                this.setOrderedToSit(true);
                this.setParent(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                if (player instanceof ServerPlayer sp) {
                    FncAdvancements.grant(sp, "taming/root");
                    FncAdvancements.grant(sp, "taming/cozy_nest");
                }
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
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new BabyOwlbearSleepGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2, 8.0F, 3.0F));
        this.goalSelector.addGoal(5, new BabyOwlbearFollowParentGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // Sound

    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.BABY_OWLBEAR_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.BABY_OWLBEAR_DEATH.get(); }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (++this.growthTick >= GROW_UP_TICKS) growUp();
    }

    // Grow up

    protected EntityType<? extends OwlbearEntity> adultType() {
        return EntityRegistry.OWLBEAR.get();
    }

    private void growUp() {
        if (!(this.level() instanceof ServerLevel server)) return;
        OwlbearEntity adult = adultType().create(server);
        if (adult == null) return;

        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(adult.blockPosition()),
                MobSpawnType.CONVERSION, null);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growthTick);
        if (parentUuid != null) tag.putUUID("ParentUUID", parentUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        growthTick = tag.getInt("Growth");
        if (tag.hasUUID("ParentUUID")) parentUuid = tag.getUUID("ParentUUID");
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving())   return state.setAndContinue(WALK_ANIM);
            if (this.isSleepPose()) return state.setAndContinue(SLEEP_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
