package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.goal.BetterPathNavigation;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GolemEntity extends Monster implements GeoEntity {

    // Variables

    public static final int STATE_NORMAL     = 0;
    public static final int STATE_FALLING    = 1;
    public static final int STATE_VULNERABLE = 2;
    public static final int STATE_GETTING_UP = 3;

    private static final int    ATTACK_HIT_TICK     = 24;
    private static final int    ATTACK_TOTAL_TICKS  = 30;
    private static final int    WAKE_UP_TOTAL_TICKS = 70;
    private static final float  BODY_HEALTH_MAX     = 40f;
    private static final int    FALL_ANIM_TICKS     = 20;
    private static final int    VULNERABLE_TICKS    = 100;
    private static final int    GET_UP_ANIM_TICKS   = 30;
    private static final int    DEATH_ANIM_TICKS    = 60;

    private static final int    THROW_HIT_TICK      = 21;
    private static final int    THROW_TOTAL_TICKS   = 30;
    private static final float  THROW_DAMAGE        = 8.0f;
    private static final float  THROW_SPEED         = 1.4f;

    private static final int    REGEN_INTERVAL      = 20;
    private static final int    REGEN_BLOCKS_NEEDED = 5;
    private static final int    REGEN_SCAN_RADIUS   = 6;
    public  static final double SLEEP_DETECTION_RANGE = 8.0;

    private static final Set<Block> REGEN_BLOCKS = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.PODZOL, Blocks.MYCELIUM
    );

    protected Set<Block> getRegenBlocks() { return REGEN_BLOCKS; }
    protected BlockState getBodyBlockState() { return Blocks.PACKED_MUD.defaultBlockState(); }

    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GOLEM_STATE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float>   BODY_HEALTH = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   HAND_SCALE  = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected Entity pendingAttackTarget  = null;
    protected int    attackDelayTick      = 0;
    private Entity pendingThrowTarget   = null;
    private int    throwDelayTick       = 0;
    private int    wakeUpTick           = 0;
    private int    phaseTimer           = 0;
    private int    deathAnimTick        = 0;
    private int    regenTickTimer       = 0;
    private int    regenBlocksSpawned   = 0;
    private int    regenBlocksArrived   = 0;

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SLEEP_ANIM        = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation VULNERABLE_ANIM   = RawAnimation.begin().thenLoop("vulnerable");
    private static final RawAnimation ATTACK_ANIM           = RawAnimation.begin().then("attack",           Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_UPPERCUT_ANIM  = RawAnimation.begin().then("attack_uppercut",  Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_THROW_ANIM = RawAnimation.begin().then("attack_throw", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation WAKE_UP_ANIM      = RawAnimation.begin().then("wake_up",      Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FALL_ANIM         = RawAnimation.begin().then("fall",         Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation GET_UP_ANIM       = RawAnimation.begin().then("get_up",       Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation DEATH_ANIM        = RawAnimation.begin().then("death",        Animation.LoopType.HOLD_ON_LAST_FRAME);

    // Spawn

    public GolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new GolemMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BetterPathNavigation(this, level);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new GolemBodyControl(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SLEEPING,  false);
        this.entityData.define(GOLEM_STATE,  STATE_NORMAL);
        this.entityData.define(BODY_HEALTH,  BODY_HEALTH_MAX);
        this.entityData.define(HAND_SCALE, 1.0f);
    }

    public boolean isSleeping()               { return this.entityData.get(IS_SLEEPING);    }
    public void    setSleeping(boolean value) { this.entityData.set(IS_SLEEPING, value);    }
    public int  getGolemState()              { return this.entityData.get(GOLEM_STATE);    }
    public void setGolemState(int state)     { this.entityData.set(GOLEM_STATE, state);    }
    public float getBodyHealth()             { return this.entityData.get(BODY_HEALTH);    }
    public void  setBodyHealth(float hp)     { this.entityData.set(BODY_HEALTH, hp);       }
    public float   getHandScale()               { return this.entityData.get(HAND_SCALE);          }
    public void    setHandScale(float v)        { this.entityData.set(HAND_SCALE, Mth.clamp(v, 0f, 1f)); }
    public boolean isMissingHand()              { return getHandScale() < 1.0f;                    }

    public void startWakingUp() {
        setSleeping(false);
        this.triggerAnim("events_controller", "wake_up");
        this.wakeUpTick = 1;
    }
    public boolean isWakingUp() { return wakeUpTick > 0; }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                                  @NotNull DifficultyInstance difficulty,
                                                  @NotNull MobSpawnType spawnType,
                                                  @Nullable SpawnGroupData spawnGroupData,
                                                  @Nullable net.minecraft.nbt.CompoundTag dataTag) {
        setSleeping(true);
        setBodyHealth(BODY_HEALTH_MAX);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               80.0)
                .add(Attributes.MOVEMENT_SPEED,            0.18)
                .add(Attributes.ATTACK_DAMAGE,             7.0)
                .add(Attributes.FOLLOW_RANGE,             24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,      0.6);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GolemSleepGoal(this));
        this.goalSelector.addGoal(2, new GolemAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    // Sound, Death

    @Override protected SoundEvent getAmbientSound()                { return SoundsRegistry.GOLEM_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src)   { return SoundsRegistry.GOLEM_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                  { return SoundsRegistry.GOLEM_DEATH.get(); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_STEP, 12.0F, 0.8F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        this.triggerAnim("events_controller", "death");
        if (!this.level().isClientSide) {
            spawnBlockPile();
        }
    }

    protected void spawnBlockPile() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos center = this.blockPosition();

        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    float e = (dx * dx + dz * dz) / 6.25f + (dy * dy) / 2.25f;
                    if (e > 1.0f) continue;
                    if (this.random.nextBoolean()) continue;
                    if (e > 0.55f && this.random.nextFloat() > 0.55f) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    if (serverLevel.getBlockState(pos).canBeReplaced()) {
                        serverLevel.setBlock(pos, Blocks.PACKED_MUD.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    // Combat

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        int state = getGolemState();
        if (state == STATE_VULNERABLE) {
            boolean damaged = super.hurt(source, amount);
            if (damaged && this.hurtTime == 0) this.hurtTime = 10;
            return damaged;
        }

        if (state == STATE_NORMAL && !level().isClientSide) {
            setBodyHealth(Math.max(0f, getBodyHealth() - amount));
            playSound(SoundEvents.MUD_HIT, 0.9f, 0.85f + this.random.nextFloat() * 0.3f);

            Entity attacker = source.getEntity();
            double px = attacker != null ? (this.getX() + attacker.getX()) * 0.5 : this.getX();
            double py = this.getY() + this.getBbHeight() * 0.55;
            double pz = attacker != null ? (this.getZ() + attacker.getZ()) * 0.5 : this.getZ();
            ((ServerLevel) level()).sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, getBodyBlockState()),
                    px, py, pz, 14, 0.25, 0.25, 0.25, 0.08);

            if (getTarget() == null && attacker instanceof LivingEntity le
                    && !(le instanceof Player p && (p.isCreative() || p.isSpectator()))) {
                setTarget(le);
            }
        }
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) { }

    public boolean isAttacking() { return attackDelayTick > 0 || throwDelayTick > 0; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        this.triggerAnim("events_controller", "attack");
        this.pendingAttackTarget = target;
        this.attackDelayTick     = 1;
        return true;
    }

    public void doThrowAttack(Entity target) {
        this.triggerAnim("events_controller", "attack_throw");
        this.pendingThrowTarget = target;
        this.throwDelayTick     = 1;
    }

    protected void onAttackLanded(Entity target) {}

    private void spawnHandProjectile() {
        if (pendingThrowTarget == null || !pendingThrowTarget.isAlive()) return;

        ServerLevel serverLevel = (ServerLevel) level();
        Vec3 targetCenter = new Vec3(
                pendingThrowTarget.getX(),
                pendingThrowTarget.getY() + pendingThrowTarget.getBbHeight() * 0.5,
                pendingThrowTarget.getZ());

        BlockProjectile proj = BlockProjectile.create(
                serverLevel, this,
                getBodyBlockState(),
                THROW_SPEED, THROW_DAMAGE, targetCenter);

        serverLevel.addFreshEntity(proj);
    }

    // Regen

    private boolean spawnRegenBlock(ServerLevel serverLevel) {
        BlockPos center = this.blockPosition();
        int r = REGEN_SCAN_RADIUS;

        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -1, -r),
                center.offset(r,   2,  r))) {
            if (pos.equals(center)) continue;
            if (getRegenBlocks().contains(serverLevel.getBlockState(pos).getBlock())) {
                candidates.add(pos.immutable());
            }
        }

        if (candidates.isEmpty()) return false;

        BlockPos chosen = candidates.get(this.random.nextInt(candidates.size()));
        BlockState state = serverLevel.getBlockState(chosen);

        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                chosen.getX() + 0.5, chosen.getY() + 0.5, chosen.getZ() + 0.5,
                8, 0.2, 0.2, 0.2, 0.1);
        serverLevel.removeBlock(chosen, false);
        this.playSound(SoundEvents.GRAVEL_STEP, 0.8f, 0.7f + this.random.nextFloat() * 0.3f);

        BlockProjectile regen = BlockProjectile.createRegenBlock(
                serverLevel, this,
                chosen.getX() + 0.5, chosen.getY() + 0.5, chosen.getZ() + 0.5,
                state);
        serverLevel.addFreshEntity(regen);
        return true;
    }

    public void onRegenBlockArrived() {
        regenBlocksArrived++;
        setHandScale((float) regenBlocksArrived / REGEN_BLOCKS_NEEDED);
        if (regenBlocksArrived >= REGEN_BLOCKS_NEEDED) {
            regenBlocksSpawned = 0;
            regenBlocksArrived = 0;
            regenTickTimer     = 0;
        }
    }

    // Tick

    @Override
    public void tick() {
        if (this.deathTime > 0 && deathAnimTick < DEATH_ANIM_TICKS) {
            this.deathTime = Math.min(this.deathTime, 18);
        }

        boolean vulnerable = getGolemState() == STATE_VULNERABLE;
        float preYRot  = this.getYRot();
        float preYHead = this.yHeadRot;
        float preYBody = this.yBodyRot;

        super.tick();

        if (this.deathTime > 0) deathAnimTick++;

        if (isSleeping()) {
            float snapped = Math.round(this.getYRot() / 90.0f) * 90.0f;
            this.setYRot(snapped);    this.yRotO     = snapped;
            this.yHeadRot  = snapped; this.yHeadRotO = snapped;
            this.yBodyRot  = snapped; this.yBodyRotO = snapped;
        }

        if (vulnerable) {
            this.setYRot(preYRot);    this.yRotO     = preYRot;
            this.yHeadRot  = preYHead; this.yHeadRotO = preYHead;
            this.yBodyRot  = preYBody; this.yBodyRotO = preYBody;
        }

        if (this.level().isClientSide) return;

        if (attackDelayTick > 0) {
            attackDelayTick++;
            if (attackDelayTick == ATTACK_HIT_TICK) {
                if (pendingAttackTarget != null
                        && pendingAttackTarget.isAlive()
                        && this.distanceTo(pendingAttackTarget) <= GolemAttackGoal.MAX_ATTACK_RANGE + 1.0
                        && this.hasLineOfSight(pendingAttackTarget)) {
                    boolean landed = super.doHurtTarget(pendingAttackTarget);
                    if (landed) {
                        ServerLevel sl = (ServerLevel) level();
                        sl.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK,
                                        getBodyBlockState()),
                                pendingAttackTarget.getX(),
                                pendingAttackTarget.getY() + pendingAttackTarget.getBbHeight() * 0.5,
                                pendingAttackTarget.getZ(),
                                24, 0.3, 0.35, 0.3, 0.18);
                        onAttackLanded(pendingAttackTarget);
                    }
                }
                pendingAttackTarget = null;
            }
            if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
        }

        if (throwDelayTick > 0) {
            throwDelayTick++;
            if (throwDelayTick == THROW_HIT_TICK) {
                spawnHandProjectile();
                setHandScale(0.0f);
                regenBlocksSpawned = 0;
                regenBlocksArrived = 0;
                regenTickTimer     = 0;
            }
            if (throwDelayTick >= THROW_TOTAL_TICKS) {
                throwDelayTick  = 0;
                pendingThrowTarget = null;
            }
        }

        if (isMissingHand() && regenBlocksSpawned < REGEN_BLOCKS_NEEDED) {
            regenTickTimer++;
            if (regenTickTimer >= REGEN_INTERVAL) {
                regenTickTimer = 0;
                if (spawnRegenBlock((ServerLevel) level())) {
                    regenBlocksSpawned++;
                }
            }
        }

        if (wakeUpTick > 0) {
            wakeUpTick++;
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));

            ServerLevel sl = (ServerLevel) level();

            if (wakeUpTick == 2)
                this.playSound(SoundEvents.WARDEN_DIG, 1.8f, 0.55f);
            if (wakeUpTick == 14)
                this.playSound(SoundEvents.GRAVEL_BREAK, 1.1f, 0.60f + random.nextFloat() * 0.1f);
            if (wakeUpTick == 28)
                this.playSound(SoundEvents.GRAVEL_BREAK, 1.3f, 0.65f + random.nextFloat() * 0.1f);
            if (wakeUpTick == 42)
                this.playSound(SoundEvents.WARDEN_DIG,   1.6f, 0.90f);
            if (wakeUpTick == 56)
                this.playSound(SoundEvents.MUD_HIT,      1.6f, 0.55f);

            if (wakeUpTick % 4 == 0) {
                float progress = Math.min(1f, wakeUpTick / (float) WAKE_UP_TOTAL_TICKS);
                int   count    = 3 + (int) (progress * 12);
                double radius  = 0.4 + progress * 1.4;

                for (int i = 0; i < count; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double r     = random.nextDouble() * radius;
                    double vy = 0.12 + random.nextDouble() * 0.28 * (0.5 + progress);
                    sl.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK,
                                    getBodyBlockState()),
                            getX() + Math.cos(angle) * r,
                            getY() + 0.1 + progress * 0.6,
                            getZ() + Math.sin(angle) * r,
                            1,
                            0.04, 0.02, 0.04,
                            vy);
                }

                if (progress > 0.3f) {
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, getBodyBlockState()),
                            getX(), getY() + 0.2, getZ(),
                            (int) (progress * 4), radius * 0.5, 0.1, radius * 0.5, 0.02);
                }
            }

            if (wakeUpTick >= WAKE_UP_TOTAL_TICKS) wakeUpTick = 0;
        }

        int state = getGolemState();

        if (state == STATE_NORMAL) {
            if (getBodyHealth() <= 0f) {
                setGolemState(STATE_FALLING);
                this.triggerAnim("events_controller", "fall");
                this.getNavigation().stop();
                phaseTimer = 0;
            }

        } else if (state == STATE_FALLING) {
            this.getNavigation().stop();
            this.setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
            phaseTimer++;
            if (phaseTimer >= FALL_ANIM_TICKS) {
                setGolemState(STATE_VULNERABLE);
                phaseTimer = 0;
            }

        } else if (state == STATE_VULNERABLE) {
            this.getNavigation().stop();
            this.setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
            phaseTimer++;
            if (phaseTimer >= VULNERABLE_TICKS && this.isAlive()) {
                setGolemState(STATE_GETTING_UP);
                this.triggerAnim("events_controller", "get_up");
                phaseTimer = 0;
            }

        } else if (state == STATE_GETTING_UP) {
            this.getNavigation().stop();
            this.setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
            phaseTimer++;
            if (phaseTimer >= GET_UP_ANIM_TICKS) {
                setBodyHealth(BODY_HEALTH_MAX);
                setGolemState(STATE_NORMAL);
                phaseTimer = 0;
            }
        }
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {

        registrar.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isSleeping())                              return state.setAndContinue(SLEEP_ANIM);
            int gs = this.getGolemState();
            if (gs == STATE_FALLING || gs == STATE_GETTING_UP) return PlayState.STOP;
            if (gs == STATE_VULNERABLE)                         return state.setAndContinue(VULNERABLE_ANIM);
            if (state.isMoving())                               return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "events_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("wake_up",         WAKE_UP_ANIM)
                .triggerableAnim("attack",          ATTACK_ANIM)
                .triggerableAnim("attack_uppercut", ATTACK_UPPERCUT_ANIM)
                .triggerableAnim("attack_throw",    ATTACK_THROW_ANIM)
                .triggerableAnim("fall",         FALL_ANIM)
                .triggerableAnim("get_up",       GET_UP_ANIM)
                .triggerableAnim("death",        DEATH_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
