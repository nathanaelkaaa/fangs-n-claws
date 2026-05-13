package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OwlbearEntity extends Monster implements GeoEntity {

    private static final int ATTACK1_HIT_TICK  = 15;
    private static final int ATTACK2_HIT_TICK  = 18;
    private static final int ATTACK_TOTAL_TICKS = 25;
    private static final int    HOWL_TOTAL_TICKS = 60;
    private static final double SPRINT_PARTICLE_SPEED_THRESHOLD = 0.05;
    private static final double SLEEP_DETECTION_RANGE = 6.0;

    private static final EntityDataAccessor<Boolean> IS_RUNNING =
            SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING =
            SynchedEntityData.defineId(OwlbearEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity  pendingAttackTarget  = null;
    private int     attackDelayTick      = 0;
    private int     currentAttackHitTick = ATTACK1_HIT_TICK;
    private boolean currentIsAttack1     = false;
    private int     howlDelayTick        = 0;

    private double prevX, prevZ;

    private static final RawAnimation IDLE_ANIM    = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM    = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM     = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation SLEEP_ANIM   = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation ATTACK1_ANIM = RawAnimation.begin().then("attack1", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK2_ANIM = RawAnimation.begin().then("attack2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation HOWL_ANIM    = RawAnimation.begin().then("howl",    Animation.LoopType.PLAY_ONCE);

    public OwlbearEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new OwlbearMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BetterPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(IS_RUNNING,  false);
        pBuilder.define(IS_SLEEPING, false);
    }

    public boolean isRunning()  { return this.entityData.get(IS_RUNNING); }
    public void setRunning(boolean running) { this.entityData.set(IS_RUNNING, running); }

    public boolean isSleeping()  { return this.entityData.get(IS_SLEEPING); }
    public void setSleeping(boolean sleeping) { this.entityData.set(IS_SLEEPING, sleeping); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OwlbearAttackGoal(this));
        this.goalSelector.addGoal(4, new OwlbearSleepGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {

            @Override
            public boolean canUse() {
                if (isSleeping()) {
                    Player nearby = level().getNearestPlayer(OwlbearEntity.this, SLEEP_DETECTION_RANGE);
                    if (nearby == null || nearby.isSpectator() || nearby.isCreative() || nearby.isCrouching())
                        return false;
                    this.target = nearby;
                    return true;
                }
                if (!super.canUse()) return false;
                return !(this.target instanceof Player p && p.isCrouching());
            }

            @Override
            public boolean canContinueToUse() {
                if (OwlbearEntity.this.getTarget() instanceof Player p && p.isCrouching()) return false;
                return super.canContinueToUse();
            }
        });
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    public boolean isAttacking() { return attackDelayTick > 0; }
    public boolean isHowling()   { return howlDelayTick   > 0; }

    public void triggerHowl() {
        this.triggerAnim("attack_controller", "howl");
        this.howlDelayTick = 1;
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.85F + this.random.nextFloat() * 0.2F);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.POLAR_BEAR_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.POLAR_BEAR_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.POLAR_BEAR_DEATH; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (this.random.nextInt(3) == 0) {
            this.triggerAnim("attack_controller", "attack1");
            this.currentAttackHitTick = ATTACK1_HIT_TICK;
            this.currentIsAttack1     = true;
        } else {
            this.triggerAnim("attack_controller", "attack2");
            this.currentAttackHitTick = ATTACK2_HIT_TICK;
            this.currentIsAttack1     = false;
        }
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    @Override
    public void tick() {
        prevX = this.getX();
        prevZ = this.getZ();

        super.tick();

        if (!this.level().isClientSide) {
            if (attackDelayTick > 0) {
                attackDelayTick++;
                if (attackDelayTick == currentAttackHitTick) {
                    if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= OwlbearAttackGoal.MAX_ATTACK_RANGE
                            && this.hasLineOfSight(pendingAttackTarget)) {
                        if (currentIsAttack1 && pendingAttackTarget instanceof LivingEntity livingTarget) {
                            livingTarget.hurt(this.damageSources().mobAttack(this), 8.0f);
                            double dx = livingTarget.getX() - this.getX();
                            double dz = livingTarget.getZ() - this.getZ();
                            double len = Math.sqrt(dx * dx + dz * dz);
                            if (len > 0) {
                                livingTarget.knockback(3.5, -dx / len, -dz / len);
                            }
                        } else {
                            super.doHurtTarget(pendingAttackTarget);
                        }
                    }
                    pendingAttackTarget = null;
                }
                if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
            }

            if (howlDelayTick > 0) {
                howlDelayTick++;
                if (howlDelayTick >= HOWL_TOTAL_TICKS) howlDelayTick = 0;
            }
        }

        if (this.level().isClientSide && this.onGround() && this.isRunning()) {
            double dx = this.getX() - prevX;
            double dz = this.getZ() - prevZ;
            if (Math.sqrt(dx * dx + dz * dz) > SPRINT_PARTICLE_SPEED_THRESHOLD) {
                spawnSprintBlockParticles();
            }
        }
    }

    private void spawnSprintBlockParticles() {
        BlockPos pos = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        BlockState state = this.level().getBlockState(pos);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    this.getX() + (this.random.nextFloat() - 0.5f) * this.getBbWidth(),
                    this.getBoundingBox().minY + 0.1,
                    this.getZ() + (this.random.nextFloat() - 0.5f) * this.getBbWidth(),
                    -this.getDeltaMovement().x * 4.0,
                    1.5,
                    -this.getDeltaMovement().z * 4.0
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 10, state -> {
            if (this.isSleeping()) return state.setAndContinue(SLEEP_ANIM);
            if (state.isMoving()) {
                if (this.isRunning()) return state.setAndContinue(RUN_ANIM);
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 3, state -> PlayState.STOP)
                .triggerableAnim("attack1", ATTACK1_ANIM)
                .triggerableAnim("attack2", ATTACK2_ANIM)
                .triggerableAnim("howl",    HOWL_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
