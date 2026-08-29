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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.horse.ChargeAbility;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseChargeGoal;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseChargeMoveControl;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMob;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMobStruggleGoal;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class NightmareHorseEntity extends HorseMob {

    public static final int VARIANT_RED   = 0;
    public static final int VARIANT_BLACK = 1;

    private static final double CHARGE_SPEED      = 0.55;
    private static final double BACKUP_SPEED      = 0.12;
    private static final int    CHARGE_COOLDOWN   = 120;
    private static final int    WINDUP_TICKS      = 20;
    private static final int    CHARGE_TICKS      = 25;
    private static final double CHARGE_RANGE      = 16.0;
    private static final double CHARGE_KNOCKBACK  = 1.2;
    private static final int    CHARGE_FIRE_SECS  = 4;

    private static final float BOOST_MULT       = 1.5f;
    private static final float STAMINA_DRAIN    = 0.0125f;
    private static final float STAMINA_RECHARGE = 0.0062f;
    private static final int   STAMINA_REGEN_DELAY = 40;

    private int staminaRegenDelay = 0;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STAMINA =
            SynchedEntityData.defineId(NightmareHorseEntity.class, EntityDataSerializers.FLOAT);

    private static final ResourceLocation TEX_RED   = FangsClawsMod.id("textures/entity/nightmare_horse.png");
    private static final ResourceLocation TEX_BLACK = FangsClawsMod.id("textures/entity/nightmare_horse_black.png");

    private ChargeAbility charge;

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_SLOW_ANIM    = RawAnimation.begin().thenLoop("walk_slow");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM          = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation EATING_GRASS_ANIM = RawAnimation.begin().thenLoop("eating_grass");

    public NightmareHorseEntity(EntityType<?> type, Level level) {
        super((EntityType<? extends AbstractHorse>) type, level);
        if (this.charge == null) this.charge = createCharge(); // côté client, registerGoals() n'a pas tourné
        this.moveControl = new HorseChargeMoveControl(this, this.charge);
    }

    private ChargeAbility createCharge() {
        return new ChargeAbility(this, CHARGE_SPEED, BACKUP_SPEED, CHARGE_COOLDOWN, WINDUP_TICKS,
                CHARGE_TICKS, CHARGE_RANGE, CHARGE_KNOCKBACK, CHARGE_FIRE_SECS,
                this::spawnHeadSmoke, this::spawnLegFire);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_RED);
        builder.define(STAMINA, 1.0F);
    }

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

    public int  getVariant()      { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, v); }

    @Override
    public ResourceLocation textureLocation() {
        return this.getVariant() == VARIANT_BLACK ? TEX_BLACK : TEX_RED;
    }

    @Override
    public boolean isHealingItem(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE);
    }

    public float getStamina()        { return this.entityData.get(STAMINA); }
    public void  setStamina(float v) { this.entityData.set(STAMINA, Mth.clamp(v, 0.0F, 1.0F)); }

    private boolean isBoosting() {
        return this.getControllingPassenger() instanceof Player p && p.isSprinting() && this.getStamina() > 0.0F;
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.charge = createCharge();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HorseMobStruggleGoal(this));
        this.goalSelector.addGoal(2, new HorseChargeGoal(this, this.charge));
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
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Nullable
    @Override
    protected Mob createRider(ServerLevel level) {
        return level.random.nextBoolean()
                ? EntityType.WITHER_SKELETON.create(level)
                : EntityType.SKELETON.create(level);
    }

    // Particles

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

    // Boost
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

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.charge.tickCooldown();

            boolean sprinting = this.getControllingPassenger() instanceof Player p && p.isSprinting();
            if (sprinting && this.getStamina() > 0.0F) {
                this.setStamina(this.getStamina() - STAMINA_DRAIN);
                this.staminaRegenDelay = STAMINA_REGEN_DELAY;
                this.spawnLegFire();
                this.applyBoostHits();
            } else if (this.staminaRegenDelay > 0) {
                // Delai avant que la recharge ne s'amorce : s'arreter une demi-seconde ne
                // suffit plus a recuperer.
                this.staminaRegenDelay--;
            } else {
                // La recharge n'est plus conditionnee au fait que le joueur bouge ou non :
                // une fois le delai ecoule elle tourne en continu, sprint mis a part.
                this.setStamina(this.getStamina() + STAMINA_RECHARGE);
            }
        }
    }


    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level().isClientSide) {
            this.triggerAnim("attack_controller", "rear");
        }
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        float base = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        return this.isBoosting() ? base * BOOST_MULT : base;
    }

    // Sons

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
        this.addAttackController(registrar);

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
}
