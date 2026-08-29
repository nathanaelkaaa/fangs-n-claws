package net.raptorzizi.fangs_n_claws.entity.skull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SkullEntity extends Monster implements GeoEntity {

    private static final double CHASE_SPEED         = 0.35;
    private static final double RUSH_SPEED          = 0.60;
    private static final double FLY_SPEED_ATTRIBUTE = 0.20;

    private static final double ARRIVE_THRESHOLD    = 1.8;
    private static final int    TARGET_MAX_COOLDOWN = 80;
    private static final int    ATTACK_HIT_TICK     = 6;
    private static final int    RUSH_MAX_TICKS      = 20;
    private static final int    ATTACK_COOLDOWN_MAX = 40;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Vec3 flyTarget      = null;
    private int  targetCooldown = 0;
    private int  rushTick       = 0;
    private int  attackCooldown = 0;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation RUN_ANIM  = RawAnimation.begin().thenLoop("run");

    public SkullEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,                8.0)
                .add(Attributes.MOVEMENT_SPEED,            0.0)
                .add(Attributes.ATTACK_DAMAGE,             3.0)
                .add(Attributes.FOLLOW_RANGE,             28.0)
                .add(Attributes.FLYING_SPEED,             FLY_SPEED_ATTRIBUTE)
                .add(Attributes.ENTITY_INTERACTION_RANGE,  1.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Variations

    public abstract String textureBaseName();

    @Nullable
    public ResourceLocation glowingEyesTexture() { return null; }

    protected void applyHitEffect(LivingEntity target) { }

    protected void spawnAmbientParticles() { }

    private static final double PARTICLE_BACK_OFFSET = 0.45;

    protected final void emitAround(SimpleParticleType particle, double speed) {
        Vec3 back = this.getLookAngle().scale(-PARTICLE_BACK_OFFSET);
        this.level().addParticle(particle,
                this.getX() + back.x + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 0.5,
                this.getY() + back.y + this.random.nextDouble() * this.getBbHeight(),
                this.getZ() + back.z + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 0.5,
                0.0, speed, 0.0);
    }

    // Sons

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.SKELETON_DEATH; }
    @Override protected float getSoundVolume()                    { return 0.4F; }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return source.is(DamageTypes.FLY_INTO_WALL) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    // Combat

    public boolean isAttacking() { return rushTick > 0; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (isAttacking() || attackCooldown > 0) return false;
        rushTick = 1;
        return true;
    }

    private void tickAttack(LivingEntity target) {
        rushTick++;

        Vec3 rushTarget = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
        faceToward(rushTarget);
        steerToward(rushTarget, RUSH_SPEED, 0.35);

        if (rushTick == ATTACK_HIT_TICK) {
            if (target.isAlive()
                    && this.distanceTo(target) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 1.0
                    && super.doHurtTarget(target)) {
                this.applyHitEffect(target);
            }
        }

        if (rushTick > ATTACK_HIT_TICK || rushTick > RUSH_MAX_TICKS) {
            rushTick = 0;
            attackCooldown = ATTACK_COOLDOWN_MAX;
        }
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.spawnAmbientParticles();
            return;
        }

        if (attackCooldown > 0) attackCooldown--;

        LivingEntity target = this.getTarget();

        if (isAttacking()) {
            if (target != null && target.isAlive()) tickAttack(target);
            else rushTick = 0;
            return;
        }

        if (target != null && target.isAlive()) {
            flyTarget = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            faceToward(flyTarget);

            if (this.distanceTo(target) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                    && attackCooldown == 0) {
                doHurtTarget(target);
            } else {
                steerToward(flyTarget, CHASE_SPEED, 0.15);
            }
            return;
        }

        targetCooldown--;
        if (flyTarget == null || targetCooldown <= 0
                || this.position().distanceTo(flyTarget) < ARRIVE_THRESHOLD) {
            flyTarget      = pickRandomFlyTarget();
            targetCooldown = TARGET_MAX_COOLDOWN;
        }

        if (flyTarget != null) faceToward(flyTarget);
        steerToward(flyTarget, this.getAttributeValue(Attributes.FLYING_SPEED), 0.12);

        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.1, 0));
            flyTarget = null;
        }
    }

    private void steerToward(Vec3 target, double speed, double lerpFactor) {
        if (target == null) return;
        Vec3 diff = target.subtract(this.position());
        if (diff.length() > 0.2) {
            Vec3 wanted = diff.normalize().scale(speed);
            this.setDeltaMovement(this.getDeltaMovement().lerp(wanted, lerpFactor));
        }
    }

    private void faceToward(Vec3 target) {
        double dx = target.x - this.getX();
        double dy = target.y - (this.getY() + this.getBbHeight() * 0.5);
        double dz = target.z - this.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist * hDist + dy * dy < 0.01) return;

        float yaw   = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Mth.atan2(dy, hDist) * Mth.RAD_TO_DEG);

        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    private Vec3 pickRandomFlyTarget() {
        double range = 8.0;
        double tx = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * range;
        double ty = this.getY() + (this.random.nextDouble() * 2.0 - 1.0) * (range * 0.5);
        double tz = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * range;

        ty = Math.max(this.level().getMinBuildHeight() + 2,
                Math.min(ty, this.level().getMaxBuildHeight() - 2));

        if (this.level().getBlockState(BlockPos.containing(tx, ty, tz)).blocksMotion()) return null;
        return new Vec3(tx, ty, tz);
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state", 3, state -> {
            LivingEntity target = this.getTarget();
            if (this.isAttacking() || (target != null && target.isAlive())) {
                return state.setAndContinue(RUN_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
