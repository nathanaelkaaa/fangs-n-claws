package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BabyScorpionEntity extends Monster implements GeoEntity {

    public static final int VARIANT_NORMAL = 0;
    public static final int VARIANT_DESERT = 1;
    public static final int VARIANT_FROST  = 2;
    public static final int VARIANT_NETHER = 3;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BabyScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RIDE_YAW =
            SynchedEntityData.defineId(BabyScorpionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> HEAD_OWNER_ID =
            SynchedEntityData.defineId(BabyScorpionEntity.class, EntityDataSerializers.INT);

    private static final int GROW_TICKS = 24000;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private UUID parentUuid;
    private ScorpionEntity parentCache;
    private UUID ownerUuid;
    private int growTimer = GROW_TICKS;
    private boolean pendingRemount = false;
    public BabyScorpionEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH,        6.0)
                .add(Attributes.MOVEMENT_SPEED,    0.25)
                .add(Attributes.ATTACK_DAMAGE,     1.0)
                .add(Attributes.FOLLOW_RANGE,     20.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, VARIANT_NORMAL);
        this.entityData.define(RIDE_YAW, 0.0f);
        this.entityData.define(HEAD_OWNER_ID, -1);
    }

    public int  getVariant()            { return this.entityData.get(VARIANT); }
    public void setVariant(int variant) { this.entityData.set(VARIANT, variant); }

    public float getRideYaw()          { return this.entityData.get(RIDE_YAW); }
    public void  setRideYaw(float yaw) { this.entityData.set(RIDE_YAW, yaw); }

    // Interact

    public int  getHeadOwnerId()       { return this.entityData.get(HEAD_OWNER_ID); }
    public void setHeadOwnerId(int id) { this.entityData.set(HEAD_OWNER_ID, id); }
    public boolean isOnHead()          { return getHeadOwnerId() != -1; }

    @Override
    public boolean isPushable() {
        return !isOnHead() && !isPassenger() && super.isPushable();
    }
    public boolean isTamed()           { return ownerUuid != null; }

    public void setOwner(Player player) {
        this.ownerUuid = player.getUUID();
        this.setPersistenceRequired();
    }

    public void mountHead(Player player) {
        if (this.isPassenger()) this.stopRiding();
        if (!this.level().isClientSide) {
            for (BabyScorpionEntity other : this.level().getEntitiesOfClass(
                    BabyScorpionEntity.class, player.getBoundingBox().inflate(2.0))) {
                if (other != this && other.getHeadOwnerId() == player.getId()) other.dismountHead();
            }
        }
        boolean firstTame = !isTamed();
        setOwner(player);
        setHeadOwnerId(player.getId());
        this.setNoGravity(true);
        this.noPhysics = true;
        if (firstTame && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    this.getX(), this.getY() + 0.4, this.getZ(),
                    7, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public void dismountHead() {
        setHeadOwnerId(-1);
        this.setNoGravity(false);
        this.noPhysics = false;
    }

    private static final double HEAD_HEIGHT = 1.9;

    private void positionOnHead(Player player) {
        double y = player.getY() + HEAD_HEIGHT;
        this.setPos(player.getX(), y, player.getZ());
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();
        float yaw = player.getYRot();
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(0);
        this.fallDistance = 0;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !this.isPassenger()
                && !isOnHead() && !player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                mountHead(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !isTamed();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (pendingRemount && ownerUuid != null) {
                Player owner = this.level().getPlayerByUUID(ownerUuid);
                if (owner != null) {
                    mountHead(owner);
                    pendingRemount = false;
                }
            }
            if (growTimer > 0 && --growTimer <= 0) {
                growUp();
                return;
            }
        }

        if (isOnHead()) {
            Entity e = this.level().getEntity(getHeadOwnerId());
            if (e instanceof Player player && player.isAlive() && !player.isSpectator()) {
                if (player.isShiftKeyDown()) {
                    if (!this.level().isClientSide) dismountHead();
                } else {
                    positionOnHead(player);
                }
            } else if (!this.level().isClientSide) {
                dismountHead();
            }
        }
    }

    public static int variantOf(ScorpionEntity scorpion) {
        if (scorpion instanceof NetherScorpionEntity) return VARIANT_NETHER;
        if (scorpion instanceof FrostScorpionEntity)  return VARIANT_FROST;
        if (scorpion instanceof DesertScorpionEntity) return VARIANT_DESERT;
        return VARIANT_NORMAL;
    }

    public ResourceLocation textureLocation() {
        return switch (getVariant()) {
            case VARIANT_DESERT -> FangsClawsMod.id("textures/entity/baby_desert_scorpion.png");
            case VARIANT_FROST  -> FangsClawsMod.id("textures/entity/baby_snow_scorpion.png");
            case VARIANT_NETHER -> FangsClawsMod.id("textures/entity/baby_nether_scorpion.png");
            default             -> FangsClawsMod.id("textures/entity/baby_scorpion.png");
        };
    }

    // Parent

    public void setParent(@Nullable ScorpionEntity parent) {
        this.parentCache = parent;
        this.parentUuid  = parent == null ? null : parent.getUUID();
    }

    @Nullable
    public ScorpionEntity getParent() {
        if (parentCache != null && parentCache.isAlive()) return parentCache;
        if (parentUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(parentUuid);
            if (e instanceof ScorpionEntity scorpion) {
                parentCache = scorpion;
                return scorpion;
            }
        }
        return null;
    }

    private void growUp() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        ScorpionEntity adult = switch (getVariant()) {
            case VARIANT_DESERT -> EntityRegistry.DESERT_SCORPION.get().create(serverLevel);
            case VARIANT_FROST  -> EntityRegistry.FROST_SCORPION.get().create(serverLevel);
            case VARIANT_NETHER -> EntityRegistry.NETHER_SCORPION.get().create(serverLevel);
            default             -> EntityRegistry.SCORPION.get().create(serverLevel);
        };
        if (adult == null) return;

        this.stopRiding();
        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        adult.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(adult.blockPosition()),
                MobSpawnType.CONVERSION, null, null);
        if (this.ownerUuid != null) adult.setOwner(this.ownerUuid);
        serverLevel.addFreshEntity(adult);
        this.discard();
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new BabyScorpionFollowParentGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // Sound

    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.SCORPION_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.SCORPION_DEATH.get(); }

    // NBT

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putFloat("RideYaw", getRideYaw());
        tag.putInt("Age", growTimer);
        tag.putBoolean("OnHead", isOnHead());
        if (parentUuid != null) tag.putUUID("ParentUUID", parentUuid);
        if (ownerUuid  != null) tag.putUUID("OwnerUUID",  ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        setRideYaw(tag.getFloat("RideYaw"));
        if (tag.contains("Age")) growTimer = tag.getInt("Age");
        if (tag.hasUUID("ParentUUID")) parentUuid = tag.getUUID("ParentUUID");
        if (tag.hasUUID("OwnerUUID"))  ownerUuid  = tag.getUUID("OwnerUUID");

        this.setNoGravity(false);
        this.noPhysics = false;
        this.setHeadOwnerId(-1);
        this.pendingRemount = tag.getBoolean("OnHead");
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "static", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
