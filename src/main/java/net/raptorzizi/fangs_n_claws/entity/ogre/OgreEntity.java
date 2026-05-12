package net.raptorzizi.fangs_n_claws.entity.ogre;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OgreEntity extends Monster implements GeoEntity {

    private static final int    ATTACK_HIT_TICK    = 15;
    private static final int    ATTACK_TOTAL_TICKS = 20;

    private static final int    SLAM_HIT_TICK    = 20;
    private static final int    SLAM_TOTAL_TICKS = 32;
    private static final double SLAM_RADIUS      = 2.5;
    private static final double SLAM_DAMAGE      = 6.0;

    private static final double SPRINT_PARTICLE_SPEED_THRESHOLD = 0.05;

    private static final EntityDataAccessor<Boolean> IS_RUNNING =
            SynchedEntityData.defineId(OgreEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    private int  slamDelayTick = 0;
    private Vec3 slamImpactPos = null;

    private double prevX, prevZ;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM    = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack",      Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SLAM_ANIM   = RawAnimation.begin().then("slam_attack", Animation.LoopType.PLAY_ONCE);

    public OgreEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new OgreMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BetterPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(IS_RUNNING, false);
    }

    public boolean isRunning() { return this.entityData.get(IS_RUNNING); }
    public void setRunning(boolean running) { this.entityData.set(IS_RUNNING, running); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OgreAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    public boolean isAttacking() { return attackDelayTick > 0; }
    public boolean isSlamming()  { return slamDelayTick  > 0; }

    @Override protected SoundEvent getAmbientSound() { return SoundsRegistry.OGRE_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource src) { return SoundsRegistry.OGRE_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundsRegistry.OGRE_DEATH.get(); }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        this.triggerAnim("attack_controller", "attack");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    public void triggerSlam(Vec3 impactPos) {
        this.triggerAnim("attack_controller", "slam_attack");
        this.slamImpactPos = impactPos;
        this.slamDelayTick = 1;
    }

    @Override
    public void tick() {
        prevX = this.getX();
        prevZ = this.getZ();

        super.tick();

        if (!this.level().isClientSide) {
            if (attackDelayTick > 0) {
                attackDelayTick++;
                if (attackDelayTick == ATTACK_HIT_TICK) {
                    if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= OgreAttackGoal.MAX_ATTACK_RANGE
                            && this.hasLineOfSight(pendingAttackTarget)) {
                        super.doHurtTarget(pendingAttackTarget);
                    }
                    pendingAttackTarget = null;
                }
                if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
            }

            if (slamDelayTick > 0) {
                slamDelayTick++;
                if (slamDelayTick == SLAM_HIT_TICK) {
                    executeSlamImpact();
                }
                if (slamDelayTick >= SLAM_TOTAL_TICKS) {
                    slamDelayTick = 0;
                    slamImpactPos = null;
                }
            }
        }

        if (this.level().isClientSide && this.onGround()) {
            double dx = this.getX() - prevX;
            double dz = this.getZ() - prevZ;
            if (Math.sqrt(dx * dx + dz * dz) > SPRINT_PARTICLE_SPEED_THRESHOLD) {
                spawnSprintBlockParticles();
            }
        }
    }

    private void executeSlamImpact() {
        if (slamImpactPos == null) return;
        ServerLevel serverLevel = (ServerLevel) this.level();

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            int r = (int) Math.ceil(SLAM_RADIUS);
            BlockPos center = BlockPos.containing(slamImpactPos);
            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-r, -1, -r),
                    center.offset(r,  r,  r))) {
                double effectiveRadius = SLAM_RADIUS * (0.7f + this.random.nextFloat() * 0.6f);
                if (Math.sqrt(center.distSqr(pos)) > effectiveRadius) continue;
                BlockState state = serverLevel.getBlockState(pos);
                if (!state.isAir() && state.getDestroySpeed(serverLevel, pos) >= 0) {
                    serverLevel.destroyBlock(pos.immutable(), true, this);
                }
            }
        }

        AABB box = new AABB(slamImpactPos, slamImpactPos).inflate(SLAM_RADIUS);
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity != this) {
                entity.hurt(this.damageSources().mobAttack(this), (float) SLAM_DAMAGE);
            }
        }

        serverLevel.playSound(null,
                slamImpactPos.x, slamImpactPos.y, slamImpactPos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                1F, 0.55F + this.random.nextFloat() * 0.2F);

        for (int i = 0; i < 35; i++) {
            double vx = (this.random.nextDouble() - 0.5) * 2.0;
            double vy = this.random.nextDouble() * 0.8 + 0.2;
            double vz = (this.random.nextDouble() - 0.5) * 2.0;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    slamImpactPos.x, slamImpactPos.y + 0.3, slamImpactPos.z,
                    0, vx, vy, vz, 0.15);
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
        registrar.add(new AnimationController<>(this, "movement", 4, state -> {
            if (state.isMoving()) {
                if (this.isRunning()) return state.setAndContinue(RUN_ANIM);
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack",      ATTACK_ANIM)
                .triggerableAnim("slam_attack", SLAM_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
