package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
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

public abstract class HorseMob extends AbstractHorse implements GeoEntity, Enemy {

    protected static final int    REJECT_HIT_TICK    = 12;
    protected static final int    REJECT_TOTAL_TICKS = 20;
    protected static final double REJECT_KNOCKBACK   = 2.0;

    protected static final int REAR_HIT_TICK    = 5;
    protected static final int REAR_TOTAL_TICKS = 25;

    private static final EntityDataAccessor<Boolean> STURDY_SADDLED =
            SynchedEntityData.defineId(HorseMob.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> ARMOR =
            SynchedEntityData.defineId(HorseMob.class, EntityDataSerializers.ITEM_STACK);

    private static final ResourceLocation ARMOR_MODIFIER_ID = FangsClawsMod.id("horse_blanket_armor");
    private static final double           BLANKET_ARMOR     = 3.0;

    protected static final RawAnimation REAR_ANIM   = RawAnimation.begin().then("rear",   Animation.LoopType.PLAY_ONCE);
    protected static final RawAnimation REJECT_ANIM = RawAnimation.begin().then("reject", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int    tamingBuckCounter = 0;
    private int    rejectTick        = 0;
    private Player tamingPlayer      = null;
    private Vec3   pendingKick       = null;

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    protected HorseMob(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
    }

    // Sync data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STURDY_SADDLED, false);
        builder.define(ARMOR, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SturdySaddled", this.isSaddled());
        if (!this.getArmor().isEmpty()) {
            tag.put("HorseArmor", this.getArmor().save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSturdySaddled(tag.getBoolean("SturdySaddled"));
        if (tag.contains("HorseArmor", 10)) {
            this.setArmor(ItemStack.parseOptional(this.registryAccess(), tag.getCompound("HorseArmor")));
        } else {
            this.setArmor(ItemStack.EMPTY);
        }
    }

    public void setSturdySaddled(boolean v) { this.entityData.set(STURDY_SADDLED, v); }

    @Override
    public boolean isSaddled() { return this.entityData.get(STURDY_SADDLED); }


    public ItemStack getArmor() { return this.entityData.get(ARMOR); }

    public void setArmor(ItemStack stack) {
        this.entityData.set(ARMOR, stack);
        this.updateArmorAttribute();
    }

    public boolean isWearingArmor() { return !this.getArmor().isEmpty(); }

    private void grantTameAdvancements() {
        if (!(this.tamingPlayer instanceof ServerPlayer sp)) return;
        FncAdvancements.grant(sp, "taming/root");
        var type = this.getType();
        if (type == EntityRegistry.HORSE_BAT.get()) {
            FncAdvancements.grant(sp, "taming/full_stable", "horse_bat");
        } else if (type == EntityRegistry.NIGHTMARE_HORSE.get()) {
            FncAdvancements.grant(sp, "taming/full_stable", "nightmare_horse");
        } else if (type == EntityRegistry.SKELETON_HORSE_MOB.get()) {
            FncAdvancements.grant(sp, "taming/full_stable", "skeleton_horse");
        } else if (type == EntityRegistry.ZOMBIE_HORSE_MOB.get()) {
            FncAdvancements.grant(sp, "taming/full_stable", "zombie_horse");
        }
    }

    public boolean isBodyArmorItem(ItemStack stack) {
        return stack.is(ItemsRegistry.HORSE_BLANKET.get());
    }

    private void updateArmorAttribute() {
        AttributeInstance inst = this.getAttribute(Attributes.ARMOR);
        if (inst == null) return;
        inst.removeModifier(ARMOR_MODIFIER_ID);
        if (this.isWearingArmor()) {
            inst.addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_ID, BLANKET_ARMOR, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public abstract ResourceLocation textureLocation();

    @Nullable
    public ResourceLocation eyesTexture() {
        return null;
    }

    public boolean isStruggling() {
        return rejectTick == 0 && !this.isTamed() && this.getFirstPassenger() instanceof Player;
    }

    @Override
    public int getMaxTemper() {
        return 200;
    }

    // Spawn

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (level instanceof ServerLevel serverLevel && spawnType != MobSpawnType.TRIGGERED
                && shouldSpawnRider(serverLevel.random)) {
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
        return random.nextFloat() < 0.9f;
    }

    @Nullable
    protected Mob createRider(ServerLevel level) {
        return null;
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.tickTaming();
            this.tickRearAttack();
        }
    }

    protected void tickTaming() {
        if (tamingBuckCounter > 0) {
            if (this.isTamed() || tamingPlayer == null || tamingPlayer.getVehicle() != this) {
                tamingBuckCounter = 0;
                tamingPlayer = null;
            } else if (--tamingBuckCounter == 0) {
                this.modifyTemper(5);
                if (this.random.nextInt(this.getMaxTemper()) < this.getTemper()) {
                    this.tameWithName(tamingPlayer);
                    grantTameAdvancements();
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
    }

    protected void tickRearAttack() {
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

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0) return true;
        this.triggerAnim("attack_controller", "rear");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    // Overrides AbstractHorse

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
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider((id, inv, p) -> new HorseArmorMenu(id, inv, this), this.getDisplayName()),
                    buf -> buf.writeInt(this.getId()));
        }
    }

    @Override
    public boolean dismountsUnderwater() {
        return true;
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

        if (this.isTamed() && player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isVehicle()) return InteractionResult.PASS;

        if (this.isHealingItem(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide) {
                this.heal(this.feedHealAmount());
                this.playSound(SoundEvents.HORSE_EAT, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() * 0.7,
                            this.getZ(), 4, this.getBbWidth() * 0.5, 0.3, this.getBbWidth() * 0.5, 0.02);
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTamed() && !this.isSaddled() && stack.is(ItemsRegistry.STURDY_SADDLE.get())) {
            if (!this.level().isClientSide) {
                this.setSturdySaddled(true);
                this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTamed() && !this.isWearingArmor() && this.isBodyArmorItem(stack)) {
            if (!this.level().isClientSide) {
                this.setArmor(stack.copyWithCount(1));
                this.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
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

    public boolean isHealingItem(ItemStack stack) {
        return false;
    }

    protected float feedHealAmount() {
        return 4.0f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(new ItemStack(ItemsRegistry.STURDY_SADDLE.get()));
            this.setSturdySaddled(false);
        }
        if (!this.getArmor().isEmpty()) {
            this.spawnAtLocation(this.getArmor());
            this.setArmor(ItemStack.EMPTY);
        }
    }

    // GeckoLib

    protected void addAttackController(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(
                new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                        .triggerableAnim("rear", REAR_ANIM)
                        .triggerableAnim("reject", REJECT_ANIM)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
