package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.projectile.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DartGoblinEntity extends Monster implements GeoEntity {

    private static final double ATTACK_RANGE        = 14.0;
    private static final double CHASE_SPEED         = 0.38;
    private static final double FLEE_SPEED          = 1.5;
    private static final int    ATTACK_COOLDOWN_MAX = 25;
    private static final int    ATTACK_HIT_TICK     = 13;
    private static final int    ATTACK_TOTAL_TICKS  = 20;
    private static final int    FLEE_TICKS          = 40;
    private static final double FLEE_STOP_DIST      = 10.0;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int          attackTick     = 0;
    private int          attackCooldown = 0;
    private int          fleeTick       = 0;
    private Vec3         fleeFromPos    = null;
    private LivingEntity pendingTarget  = null;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM    = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);

    // Constructor

    public DartGoblinEntity(EntityType<? extends DartGoblinEntity> type, Level level) {
        super(type, level);
    }

    // Attributes

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,      8.0)
                .add(Attributes.MOVEMENT_SPEED,  0.28)
                .add(Attributes.ATTACK_DAMAGE,   2.0)
                .add(Attributes.FOLLOW_RANGE,    20.0);
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Sounds

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.GOBLIN_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.GOBLIN_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.GOBLIN_DEATH.get(); }
    @Override protected float getSoundVolume()                    { return 0.8F; }

    // Hurt

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
            fleeFromPos = attacker != null ? attacker.position() : this.position().add(this.getDeltaMovement().scale(-5));
            fleeTick    = FLEE_TICKS;
            attackTick  = 0;
            pendingTarget = null;
        }
        return hurt;
    }

    // Ranged attack

    public boolean isAttacking() { return attackTick > 0; }

    private void triggerAttack(LivingEntity target) {
        if (isAttacking() || attackCooldown > 0) return;
        this.triggerAnim("attack_controller", "attack");
        pendingTarget = target;
        attackTick    = 1;
    }

    private void shootDart(LivingEntity target) {
        PoisonousDartEntity dart = new PoisonousDartEntity(EntityRegistry.POISONOUS_DART.get(), this.level());
        dart.setOwner(this);
        dart.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        double dx   = target.getX() - this.getX();
        double dy   = target.getY(0.3333) - this.getEyeY();
        double dz   = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        dart.shoot(dx, dy + dist * 0.2, dz, 1.6F, 8.0F);
        this.level().addFreshEntity(dart);
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (attackCooldown > 0) attackCooldown--;

        if (attackTick > 0) {
            attackTick++;
            if (attackTick == ATTACK_HIT_TICK && pendingTarget != null && pendingTarget.isAlive()) {
                shootDart(pendingTarget);
                pendingTarget = null;
            }
            if (attackTick >= ATTACK_TOTAL_TICKS) {
                attackTick     = 0;
                attackCooldown = ATTACK_COOLDOWN_MAX;
            }
            return;
        }

        if (fleeTick > 0) {
            fleeTick--;
            tickFlee();
            return;
        }

        tickCombat();
    }

    private void tickFlee() {
        if (fleeFromPos == null) return;

        if (this.position().distanceTo(fleeFromPos) >= FLEE_STOP_DIST) {
            fleeTick    = 0;
            fleeFromPos = null;
            this.getNavigation().stop();
            return;
        }

        Vec3 fleeDir    = this.position().subtract(fleeFromPos).normalize();
        Vec3 fleeTarget = this.position().add(fleeDir.scale(8.0));
        this.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, FLEE_SPEED);
    }

    private void tickCombat() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        double  dist   = this.distanceTo(target);
        boolean hasLos = this.hasLineOfSight(target);

        if (dist > ATTACK_RANGE || !hasLos) {
            this.getNavigation().moveTo(target, CHASE_SPEED);
        } else {
            this.getNavigation().stop();
            lookAtEntity(target);
            if (attackCooldown == 0) {
                triggerAttack(target);
            }
        }
    }

    private void lookAtEntity(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz < 0.01) return;
        float yaw = (float)(Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (isAttacking()) return PlayState.STOP;
            double speed = this.getDeltaMovement().horizontalDistance();
            if (speed > 0.18) return state.setAndContinue(RUN_ANIM);
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
}
