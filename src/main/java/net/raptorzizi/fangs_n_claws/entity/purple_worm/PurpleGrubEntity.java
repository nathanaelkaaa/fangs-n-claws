package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.projectile.AcidSplitProjectile;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterFollowOwnerGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterOwnerHurtByTargetGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterOwnerHurtTargetGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.MonsterSitGoal;
import net.raptorzizi.fangs_n_claws.entity.tame.TamableCreature;
import net.raptorzizi.fangs_n_claws.entity.tame.TamedData;
import net.raptorzizi.fangs_n_claws.entity.tame.TamedRules;
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

import java.util.Optional;
import java.util.UUID;

public class PurpleGrubEntity extends Monster implements GeoEntity, RangedAttackMob, TamableCreature {

    // Constants

    private static final int    SPIT_INTERVAL = 50;
    private static final float  SPIT_RANGE    = 16.0F;
    private static final float  SPIT_VELOCITY = 2.5F;
    private static final float  SPIT_SCALE    = 0.35F;
    private static final float  SPIT_DAMAGE   = 10.0F;
    private static final float  SPIT_SPREAD   = 0.0F;
    private static final double SPIT_GRAVITY  = 0.03;
    private static final double HEAD_HEIGHT  = 0.30;
    private static final double HEAD_FORWARD = 0.58;
    private static final int SPIT_ANIM_TICKS   = 25;
    private static final int SPIT_RELEASE_TICK = 16;
    private static final int NOT_SPITTING      = -1;
    private static final float BURROW_HEALTH_RATIO = 0.25F;
    private static final float SPIDER_EYE_HEAL           = 20.0F;
    private static final float FERMENTED_SPIDER_EYE_HEAL = 40.0F;

    private static final double FOLLOW_SPEED      = 1.2;
    private static final float  FOLLOW_START      = 5.0F;
    private static final float  FOLLOW_STOP       = 2.0F;
    private static final float  FOLLOW_TELEPORT   = 20.0F;

    // Synched data

    private static final EntityDataAccessor<Boolean> TAMED =
            SynchedEntityData.defineId(PurpleGrubEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ORDERED_TO_SIT =
            SynchedEntityData.defineId(PurpleGrubEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(PurpleGrubEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Animations

    private static final int MOVEMENT_TRANSITION = 8;
    private static final int ATTACK_TRANSITION   = 4;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT_ANIM  = RawAnimation.begin().thenLoop("sit");
    private static final RawAnimation SPIT_ANIM =
            RawAnimation.begin().then("attack_split", Animation.LoopType.PLAY_ONCE);

    // Fields

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int spitTick = NOT_SPITTING;

    @Nullable
    private LivingEntity spitTarget = null;

    // Setup

    public PurpleGrubEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,          100.0)
                .add(Attributes.ARMOR,                 8.0)
                .add(Attributes.MOVEMENT_SPEED,        0.22)
                .add(Attributes.ATTACK_DAMAGE,         3.0)
                .add(Attributes.FOLLOW_RANGE,         24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMED,          false);
        builder.define(ORDERED_TO_SIT, false);
        builder.define(OWNER,          Optional.empty());
    }

    // Taming

    @Override
    public boolean isTamed() { return this.entityData.get(TAMED); }

    @Override
    @Nullable
    public UUID getOwnerUUID() { return this.entityData.get(OWNER).orElse(null); }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
        this.entityData.set(TAMED, uuid != null);
    }

    @Override
    public boolean isOrderedToSit() { return this.entityData.get(ORDERED_TO_SIT); }

    @Override
    public void setOrderedToSit(boolean sit) { this.entityData.set(ORDERED_TO_SIT, sit); }

    // Burrowing

    public boolean isBurrowed() {
        return this.getHealth() < this.getMaxHealth() * BURROW_HEALTH_RATIO;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !isBurrowed() && super.canBeSeenAsEnemy();
    }

    @Override
    public boolean isPreventingPlayerRest(@NotNull Player player) {
        return TamedRules.preventsPlayerRest(this) && super.isPreventingPlayerRest(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return TamedRules.allowsDespawn(this) && super.removeWhenFarAway(distance);
    }

    // Interaction

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!isTamed() || !isOwnedBy(player)) return super.mobInteract(player, hand);

        ItemStack stack = player.getItemInHand(hand);
        float healed = healAmount(stack);
        if (healed > 0.0F && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.heal(healed);
            spawnHealParticles();
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!this.level().isClientSide()) {
            setOrderedToSit(!isOrderedToSit());
            this.jumping = false;
            this.getNavigation().stop();
            this.setTarget(null);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    private float healAmount(ItemStack stack) {
        if (stack.is(Items.SPIDER_EYE))           return SPIDER_EYE_HEAL;
        if (stack.is(Items.FERMENTED_SPIDER_EYE)) return FERMENTED_SPIDER_EYE_HEAL;
        return 0.0F;
    }

    private void spawnHealParticles() {
        if (!(this.level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.HEART,
                this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(),
                6, this.getBbWidth() * 0.5, 0.3, this.getBbWidth() * 0.5, 0.02);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PurpleGrubBurrowGoal(this));
        this.goalSelector.addGoal(1, new MonsterSitGoal<>(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, SPIT_INTERVAL, SPIT_RANGE));
        this.goalSelector.addGoal(3, new MonsterFollowOwnerGoal<>(this, FOLLOW_SPEED,
                FOLLOW_START, FOLLOW_STOP, FOLLOW_TELEPORT));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new MonsterOwnerHurtByTargetGoal<>(this));
        this.targetSelector.addGoal(1, new MonsterOwnerHurtTargetGoal<>(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !isTamed() && super.canUse();
            }
        });
    }

    // Combat

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float velocityFactor) {
        if (this.spitTick != NOT_SPITTING || isBurrowed()) return;

        this.triggerAnim("attack_controller", "spit");
        this.spitTarget = target;
        this.spitTick   = 0;
    }

    private void tickSpit() {
        if (this.spitTick == NOT_SPITTING) return;

        if (isBurrowed()) {
            this.spitTick   = NOT_SPITTING;
            this.spitTarget = null;
            return;
        }

        this.spitTick++;
        if (this.spitTick == SPIT_RELEASE_TICK) releaseSpit();
        if (this.spitTick >= SPIT_ANIM_TICKS) {
            this.spitTick   = NOT_SPITTING;
            this.spitTarget = null;
        }
    }

    private void releaseSpit() {
        if (this.spitTarget == null || !this.spitTarget.isAlive()) return;

        Vec3 muzzle = headPosition();
        Vec3 aim = aimAt(muzzle, this.spitTarget);

        AcidSplitProjectile spit = new AcidSplitProjectile(this.level(), this);
        spit.setScale(SPIT_SCALE);
        spit.setDamage(SPIT_DAMAGE);
        spit.setPos(muzzle.x, muzzle.y, muzzle.z);
        spit.shoot(aim.x, aim.y, aim.z, SPIT_VELOCITY, SPIT_SPREAD);
        this.level().addFreshEntity(spit);
    }

    private Vec3 aimAt(Vec3 muzzle, LivingEntity target) {
        double dx = target.getX() - muzzle.x;
        double dy = target.getY(0.5) - muzzle.y;
        double dz = target.getZ() - muzzle.z;

        double flightTicks = Math.sqrt(dx * dx + dz * dz) / SPIT_VELOCITY;
        double drop = SPIT_GRAVITY * flightTicks * flightTicks / 2.0;
        return new Vec3(dx, dy + drop, dz);
    }

    private Vec3 headPosition() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        return this.position()
                .add(0.0, HEAD_HEIGHT, 0.0)
                .add(forward.scale(HEAD_FORWARD));
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) tickSpit();
    }

    // Save data

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        TamedData.save(tag, this);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        TamedData.load(tag, this);
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", MOVEMENT_TRANSITION, state -> {
            if (this.isOrderedToSit() || this.isBurrowed()) return state.setAndContinue(SIT_ANIM);
            return state.isMoving() ? state.setAndContinue(WALK_ANIM)
                                    : state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", ATTACK_TRANSITION, state -> PlayState.STOP)
                .triggerableAnim("spit", SPIT_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
