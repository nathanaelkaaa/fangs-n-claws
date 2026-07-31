package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PurpleWormArmEntity extends Monster implements GeoEntity {

    private static final int ARM_SPAWN = 0, ARM_ATTACK_L = 1, ARM_ATTACK_R = 2, ARM_DESPAWN = 3;

    private static final int    SPAWN_TICKS      = 20;
    private static final int    ATTACK_TICKS     = 30;
    private static final int    ATTACK_HIT_TICK  = 15;
    private static final int    DESPAWN_TICKS    = 20;
    private static final double ATTACK_RANGE     = 6.0;
    private static final float  ATTACK_DAMAGE    = 8.0f;
    private static final float  ATTACK_KNOCKBACK = 0.6f;

    private static final EntityDataAccessor<Integer> ARM_STATE =
            SynchedEntityData.defineId(PurpleWormArmEntity.class, EntityDataSerializers.INT);

    private int     phaseTick = 0;
    private boolean hitDone   = false;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation SPAWN_ANIM    = RawAnimation.begin().then("spawn", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation DESPAWN_ANIM  = RawAnimation.begin().then("despawn", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation ATTACK_L_ANIM = RawAnimation.begin().then("attack_left", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation ATTACK_R_ANIM = RawAnimation.begin().then("attack_right", Animation.LoopType.HOLD_ON_LAST_FRAME);

    public PurpleWormArmEntity(EntityType<? extends PurpleWormArmEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,     20.0)
                .add(Attributes.MOVEMENT_SPEED,  0.0)
                .add(Attributes.ATTACK_DAMAGE,   ATTACK_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ARM_STATE, ARM_SPAWN);
    }

    public int  getArmState()          { return this.entityData.get(ARM_STATE); }
    private void setArmState(int state) { this.entityData.set(ARM_STATE, state); }

    // Tick
    @Override
    public void tick() {
        super.tick();
        this.faceNearestPlayer();
        if (this.level().isClientSide) return;

        switch (getArmState()) {
            case ARM_SPAWN -> {
                if (phaseTick == 0) {
                    spawnDirtBurst();
                    this.playSound(SoundsRegistry.PURPLE_WORM_ARM_EMERGE.get(), 0.9F, 1.0F);
                }
                if (++phaseTick >= SPAWN_TICKS) {
                    setArmState(this.random.nextBoolean() ? ARM_ATTACK_L : ARM_ATTACK_R);
                    phaseTick = 0;
                    hitDone = false;
                }
            }
            case ARM_ATTACK_L, ARM_ATTACK_R -> {
                phaseTick++;
                if (phaseTick == ATTACK_HIT_TICK && !hitDone) {
                    clawHit();
                    hitDone = true;
                }
                if (phaseTick >= ATTACK_TICKS) {
                    setArmState(ARM_DESPAWN);
                    phaseTick = 0;
                    spawnDirtBurst();
                    this.playSound(SoundsRegistry.PURPLE_WORM_ARM_DIG.get(), 0.9F, 1.0F);
                }
            }
            case ARM_DESPAWN -> {
                if (++phaseTick >= DESPAWN_TICKS) this.discard();
            }
        }
    }

    private void faceNearestPlayer() {
        Player p = this.level().getNearestPlayer(this, 40.0);
        if (p == null) return;
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        if (dx * dx + dz * dz < 1.0e-4) return;
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = this.yBodyRotO = yaw;
        this.yHeadRot = this.yHeadRotO = yaw;
        this.setXRot(0.0F);
    }

    private void clawHit() {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(ATTACK_RANGE))) {
            if (victim == this) continue;
            if (victim instanceof PurpleWormEntity || victim instanceof PurpleWormArmEntity) continue;
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            if (this.distanceTo(victim) > ATTACK_RANGE) continue;
            victim.hurt(this.damageSources().mobAttack(this), dmg);
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) victim.knockback(ATTACK_KNOCKBACK, -dx / len, -dz / len);
        }
        this.playSound(SoundsRegistry.PURPLE_WORM_CLAW.get(), 1.0F, 1.35F);
    }

    private void spawnDirtBurst() {
        if (!(this.level() instanceof ServerLevel server)) return;
        BlockState ground = this.level().getBlockState(this.blockPosition().below());
        if (ground.isAir()) ground = this.level().getBlockState(this.blockPosition());
        if (ground.isAir()) return;
        BlockParticleOption opt = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        server.sendParticles(opt, this.getX(), this.getY() + 0.1, this.getZ(),
                50, 0.5, 0.15, 0.5, 0.15);
    }

    @Override public boolean hurt(@NotNull DamageSource source, float amount) { return false; }
    @Override public boolean isPushable()                 { return false; }
    @Override public boolean removeWhenFarAway(double d)   { return false; }
    @Override public boolean canBeCollidedWith()          { return false; }
    @Override protected void registerGoals()               { }

    @Override protected SoundEvent getAmbientSound() { return null; }

    // GeckoLib
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 2, state -> switch (getArmState()) {
            case ARM_ATTACK_L -> state.setAndContinue(ATTACK_L_ANIM);
            case ARM_ATTACK_R -> state.setAndContinue(ATTACK_R_ANIM);
            case ARM_DESPAWN  -> state.setAndContinue(DESPAWN_ANIM);
            default           -> state.setAndContinue(SPAWN_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
