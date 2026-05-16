package net.raptorzizi.fangs_n_claws.entity.werewolf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WerewolfEntity extends Monster implements GeoEntity {

    private static final int    ATTACK_HIT_TICK    = 15;
    private static final int    ATTACK_TOTAL_TICKS = 20;
    private static final int    BITE_HIT_TICK    = 12;
    private static final int    BITE_TOTAL_TICKS = 22;
    private static final double BITE_DAMAGE      = 6.0;
    private static final int    HOWL_TOTAL_TICKS = 60;
    private static final double SPRINT_PARTICLE_SPEED_THRESHOLD = 0.05;

    private static final EntityDataAccessor<Boolean> IS_RUNNING =
            SynchedEntityData.defineId(WerewolfEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    private LivingEntity biteTarget   = null;
    private int          biteDelayTick = 0;

    private int howlDelayTick = 0;

    private double prevX, prevZ;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM    = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack",      Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation BITE_ANIM   = RawAnimation.begin().then("attack_bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation HOWL_ANIM   = RawAnimation.begin().then("howl",         Animation.LoopType.PLAY_ONCE);

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
    }

    public boolean isRunning() { return this.entityData.get(IS_RUNNING); }
    public void setRunning(boolean running) { this.entityData.set(IS_RUNNING, running); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WerewolfAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0);
    }

    public boolean isAttacking() { return attackDelayTick > 0; }
    public boolean isBiting()    { return biteDelayTick   > 0; }
    public boolean isHowling()   { return howlDelayTick   > 0; }

    public void triggerHowl() {
        this.triggerAnim("attack_controller", "owl");
        this.howlDelayTick = 1;
        this.playSound(SoundsRegistry.WEREWOLF_HOWL.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

@Override protected SoundEvent getAmbientSound() { return SoundsRegistry.WEREWOLF_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.WEREWOLF_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundsRegistry.WEREWOLF_DEATH.get(); }

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

    @Override
    public void tick() {
        prevX = this.getX();
        prevZ = this.getZ();

        super.tick();

        if (!this.level().isClientSide) {
            // Attack
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

            // Howl
            if (howlDelayTick > 0) {
                howlDelayTick++;
                if (howlDelayTick >= HOWL_TOTAL_TICKS) howlDelayTick = 0;
            }

            // Bite
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
