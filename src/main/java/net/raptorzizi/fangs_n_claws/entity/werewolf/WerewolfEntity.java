package net.raptorzizi.fangs_n_claws.entity.werewolf;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.item.SilverSwordItem;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WerewolfEntity extends Monster implements GeoEntity {

    // Variables

    private static final int    ATTACK_HIT_TICK    = 15;
    private static final int    ATTACK_TOTAL_TICKS = 20;
    private static final int    BITE_HIT_TICK      = 12;
    private static final int    BITE_TOTAL_TICKS   = 22;
    private static final double BITE_DAMAGE        = 6.0;
    private static final int    HOWL_TOTAL_TICKS   = 60;
    private static final double SPRINT_PARTICLE_SPEED_THRESHOLD = 0.05;

    private static final EntityDataAccessor<Boolean> IS_RUNNING =
            SynchedEntityData.defineId(WerewolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(WerewolfEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity      pendingAttackTarget = null;
    private int         attackDelayTick     = 0;
    private LivingEntity biteTarget         = null;
    private int          biteDelayTick      = 0;
    private int          howlDelayTick      = 0;

    private double prevX, prevZ;

    private boolean transformingBack = false;
    private int     bcounter         = 0;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM    = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack",      Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation BITE_ANIM   = RawAnimation.begin().then("attack_bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation HOWL_ANIM   = RawAnimation.begin().then("howl",        Animation.LoopType.PLAY_ONCE);

    // Spawn

    public WerewolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new WerewolfMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BetterPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(IS_RUNNING, false);
        pBuilder.define(VARIANT, 0);
    }

    public boolean isRunning()                  { return this.entityData.get(IS_RUNNING); }
    public void    setRunning(boolean running)  { this.entityData.set(IS_RUNNING, running); }

    public int  getVariant()              { return this.entityData.get(VARIANT); }
    public void setVariant(int variant)   { this.entityData.set(VARIANT, variant); }

    private boolean fleeing = false;
    public boolean isFleeing()                  { return fleeing; }
    public void    setFleeing(boolean fleeing)  { this.fleeing = fleeing; }

    @Override
    public float maxUpStep() {
        return this.isRunning() ? Math.max(1.0F, super.maxUpStep()) : super.maxUpStep();
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, SilverSkeletonEntity.class, 25.0f, 2.0, 2.0) {
            @Override public void start() { super.start(); WerewolfEntity.this.setRunning(true);  WerewolfEntity.this.setFleeing(true);  }
            @Override public void stop()  { super.stop();  WerewolfEntity.this.setRunning(false); WerewolfEntity.this.setFleeing(false); }
        });
        this.goalSelector.addGoal(2, new WerewolfAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false,
                target -> !(target instanceof WerevillagerEntity)));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,        25.0)
                .add(Attributes.MOVEMENT_SPEED,     0.2)
                .add(Attributes.ATTACK_DAMAGE,      4.0)
                .add(Attributes.FOLLOW_RANGE,       28.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        float r = this.random.nextFloat();
        if      (r < 0.50f) setVariant(0);
        else if (r < 0.85f) setVariant(1);
        else                setVariant(2);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WerewolfVariant", getVariant());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("WerewolfVariant"));
    }

    // Sound

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity directEntity = source.getDirectEntity();
        boolean hasSilverSword = directEntity instanceof Player player
                && player.getMainHandItem().getItem() instanceof SilverSwordItem;
        if (!hasSilverSword && (directEntity != null || source.getEntity() != null)) {
            amount = 1.0f;
        }
        return super.hurt(source, amount);
    }

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.WEREWOLF_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.WEREWOLF_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.WEREWOLF_DEATH.get(); }

    // Combat

    public boolean isAttacking() { return attackDelayTick > 0; }
    public boolean isBiting()    { return biteDelayTick   > 0; }
    public boolean isHowling()   { return howlDelayTick   > 0; }

    public void triggerHowl() {
        this.triggerAnim("attack_controller", "owl");
        this.howlDelayTick = 1;
        this.playSound(SoundsRegistry.WEREWOLF_HOWL.get(), 5.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        this.triggerAnim("attack_controller", "attack");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    public void triggerBite(LivingEntity target) {
        this.triggerAnim("attack_controller", "attack_bite");
        this.biteTarget    = target;
        this.biteDelayTick = 1;
    }

    private void executeBiteImpact() {
        if (biteTarget == null || !biteTarget.isAlive()) return;
        if (this.distanceTo(biteTarget) > WerewolfAttackGoal.MAX_ATTACK_RANGE + 1.5) return;

        this.playSound(SoundsRegistry.WEREWOLF_BITE.get(), 1.2F, 0.7F + this.random.nextFloat() * 0.2F);
        boolean hit = biteTarget.hurt(this.damageSources().mobAttack(this), (float) BITE_DAMAGE);

        if (hit) {
            biteTarget.addEffect(new MobEffectInstance(MobEffectsRegistry.BLEEDING, 120, 0));

            ServerLevel serverLevel = (ServerLevel) this.level();
            double tx = biteTarget.getX();
            double ty = biteTarget.getY() + biteTarget.getBbHeight() * 0.6;
            double tz = biteTarget.getZ();
            for (int i = 0; i < 18; i++) {
                double vx = (this.random.nextDouble() - 0.5) * 0.7;
                double vy = this.random.nextDouble() * 0.5 + 0.15;
                double vz = (this.random.nextDouble() - 0.5) * 0.7;
                serverLevel.sendParticles(ParticlesRegistry.BLOOD_PARTICLE.get(),
                        tx, ty, tz, 0, vx, vy, vz, 1.0);
            }
        }
    }

    // Tick

    @Override
    public void tick() {
        prevX = this.getX();
        prevZ = this.getZ();

        super.tick();

        if (!this.level().isClientSide) {
            if (this.level().isDay() && !transformingBack && this.random.nextInt(120) == 0) {
                transformingBack = true;
                bcounter = 0;
            }

            if (transformingBack) {
                bcounter++;

                double shakeOffset = (bcounter % 2 == 0) ? 0.6 : -0.6;
                this.setPos(this.getX() + shakeOffset, this.getY(), this.getZ());

                if (bcounter % 2 == 0) {
                    this.hurt(this.damageSources().magic(), 1.0F);
                }

                if (bcounter == 15) {
                    this.playSound(SoundsRegistry.WEREWOLF_HOWL.get(), 2.0F,
                            0.8F + this.random.nextFloat() * 0.3F);
                }

                if (bcounter > 30) {
                    spawnWerevillager();
                }
            }

            if (attackDelayTick > 0) {
                attackDelayTick++;
                if (attackDelayTick == ATTACK_HIT_TICK) {
                    if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= WerewolfAttackGoal.MAX_ATTACK_RANGE
                            && this.hasLineOfSight(pendingAttackTarget)) {
                        super.doHurtTarget(pendingAttackTarget);
                    }
                    pendingAttackTarget = null;
                }
                if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
            }

            if (howlDelayTick > 0) {
                howlDelayTick++;
                if (howlDelayTick >= HOWL_TOTAL_TICKS) howlDelayTick = 0;
            }

            if (biteDelayTick > 0) {
                biteDelayTick++;
                if (biteDelayTick == BITE_HIT_TICK) {
                    executeBiteImpact();
                }
                if (biteDelayTick >= BITE_TOTAL_TICKS) {
                    biteDelayTick = 0;
                    biteTarget    = null;
                }
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

    private void spawnWerevillager() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.isDeadOrDying()) return;

        for (int i = 0; i < 30; i++) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    this.getX() + (this.random.nextDouble() - 0.5),
                    this.getY() + this.getBbHeight() * 0.5 + (this.random.nextDouble() - 0.5),
                    this.getZ() + (this.random.nextDouble() - 0.5),
                    0,
                    (this.random.nextDouble() - 0.5) * 0.4,
                    this.random.nextDouble() * 0.4,
                    (this.random.nextDouble() - 0.5) * 0.4,
                    0.15);
        }

        var werevillager = EntityRegistry.WEREVILLAGER.get().create(serverLevel);
        if (werevillager != null) {
            werevillager.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            werevillager.finalizeSpawn(serverLevel,
                    serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null);
            serverLevel.addFreshEntity(werevillager);
        }

        this.discard();
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

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 10, state -> {
            if (state.isMoving()) {
                if (this.isRunning()) return state.setAndContinue(RUN_ANIM);
                return state.setAndContinue(WALK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 3, state -> PlayState.STOP)
                .triggerableAnim("attack",      ATTACK_ANIM)
                .triggerableAnim("attack_bite", BITE_ANIM)
                .triggerableAnim("owl",         HOWL_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
