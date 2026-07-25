package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.PoisonSplitProjectile;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PurpleWormEntity extends Monster implements GeoEntity {

    private static final int ATTACK_HIT_TICK    = 12;
    private static final int ATTACK_TOTAL_TICKS = 25;
    private static final int DEATH_ANIM_TICKS   = 40;

    private static final int    SPLIT_COOLDOWN_TICKS = 110;
    private static final int    SPLIT_FIRE_TICK      = 12;
    private static final int    SPLIT_TOTAL_TICKS    = 25;
    private static final double SPLIT_MIN_RANGE      = 5.0;
    private static final double SPLIT_MAX_RANGE      = 22.0;
    private static final int    SPLIT_PROJECTILES    = 1;
    private static final float  SPLIT_SPREAD_DEG     = 12.0f;
    private static final float  SPLIT_VELOCITY       = 1.2f;

    private static final int TURN_NONE = 0, R_START = 1, R_TURN = 2, R_END = 3, L_START = 4, L_TURN = 5, L_END = 6;

    private static final float TURN_ANIM_THRESHOLD = 80.0f;
    private static final float TURN_SILENT_MIN     = 45.0f;
    private static final float TURN_SPEED_FAST     = 2.0f;
    private static final float TURN_SPEED_SLOW     = 1.0f;
    private static final int   TURN_START_TICKS    = 10;
    private static final int   TURN_END_TICKS      = 20;
    private static final float SPLIT_FACING_MAX    = 35.0f;
    private static final double HEAD_HEIGHT  = 138.667 / 16.0;
    private static final double HEAD_FORWARD = 53.917 / 16.0;

    private static final EntityDataAccessor<Integer> TURN_STATE =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_YAW =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.FLOAT);

    private static final double HEAD_TRACK_HEIGHT = 11.5;
    private static final float  HEAD_TRACK_CLAMP  = 75.0f;
    private static final double LOOK_RANGE = 40.0;

    private static final double[] PART_CENTER_Y = { 1.25, 3.31, 5.22, 7.09, 8.91, 10.79, 11.97 };
    private static final double[] PART_OFFSET_Z = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.5 };
    public static final int PART_COUNT = 7;
    public static final String[] PART_BONES = { "base", "part1", "part2", "part3", "part4", "part5", "Head" };
    private final PurpleWormPart[] subParts;

    private Vec3[] animatedPartOffsets = null;
    private int    lastPartSyncTick    = -1;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;
    private int    deathAnimTick       = 0;
    private int    turnPhaseTick       = 0;

    private LivingEntity splitTarget    = null;
    private int          splitDelayTick = 0;
    private int          splitCooldown  = 0;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack_claw_right", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SPLIT_ANIM  = RawAnimation.begin().then("attack_split", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation DEATH_ANIM  = RawAnimation.begin().then("death", Animation.LoopType.HOLD_ON_LAST_FRAME);

    private static final RawAnimation TURN_R_START = RawAnimation.begin().then("turn_right_start", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation TURN_R_LOOP  = RawAnimation.begin().thenLoop("turn_right");
    private static final RawAnimation TURN_R_END   = RawAnimation.begin().then("turn_right_end", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation TURN_L_START = RawAnimation.begin().then("turn_left_start", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation TURN_L_LOOP  = RawAnimation.begin().thenLoop("turn_left");
    private static final RawAnimation TURN_L_END   = RawAnimation.begin().then("turn_left_end", Animation.LoopType.HOLD_ON_LAST_FRAME);

    public PurpleWormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;

        this.subParts = new PurpleWormPart[] {
                new PurpleWormPart(this, "base",  3.25f, 3.25f),
                new PurpleWormPart(this, "part1", 3.00f, 3.00f),
                new PurpleWormPart(this, "part2", 2.75f, 2.75f),
                new PurpleWormPart(this, "part3", 2.50f, 2.50f),
                new PurpleWormPart(this, "part4", 2.25f, 2.25f),
                new PurpleWormPart(this, "part5", 2.00f, 2.00f),
                new PurpleWormPart(this, "head",  2.75f, 2.75f),
        };
        this.setId(ENTITY_COUNTER.getAndAdd(this.subParts.length + 1) + 1);
    }

    // Multipart

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subParts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subParts.length; i++) {
            this.subParts[i].setId(id + i + 1);
        }
    }

    public boolean hurtPart(PurpleWormPart part, DamageSource source, float amount) {
        return this.hurt(source, amount);
    }

    public void setAnimatedPartOffsets(Vec3[] offsets) {
        this.animatedPartOffsets = offsets;
    }

    public int  getLastPartSyncTick()      { return this.lastPartSyncTick; }
    public void setLastPartSyncTick(int t) { this.lastPartSyncTick = t; }

    private void tickParts() {
        Vec3[] off = this.animatedPartOffsets;
        Vec3[] old = new Vec3[this.subParts.length];
        for (int i = 0; i < this.subParts.length; i++) {
            PurpleWormPart part = this.subParts[i];
            old[i] = part.position();

            double cx, cy, cz;
            if (off != null && i < off.length && off[i] != null) {
                cx = this.getX() + off[i].x;
                cy = this.getY() + off[i].y;
                cz = this.getZ() + off[i].z;
            } else {
                cx = this.getX();
                cy = this.getY() + PART_CENTER_Y[i];
                cz = this.getZ();
            }
            double lz = PART_OFFSET_Z[i];
            if (lz != 0.0) {
                float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
                cx += -Mth.sin(yawRad) * lz;
                cz +=  Mth.cos(yawRad) * lz;
            }
            part.setPos(cx, cy - part.getBbHeight() * 0.5, cz);
        }
        for (int i = 0; i < this.subParts.length; i++) {
            PurpleWormPart p = this.subParts[i];
            p.xo = p.xOld = old[i].x;
            p.yo = p.yOld = old[i].y;
            p.zo = p.zOld = old[i].z;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TURN_STATE, TURN_NONE);
        builder.define(TARGET_YAW, 0.0f);
    }

    public int   getTurnState()            { return this.entityData.get(TURN_STATE); }
    public void  setTurnState(int state)   { this.entityData.set(TURN_STATE, state); }
    public float getTargetYaw()            { return this.entityData.get(TARGET_YAW); }
    public void  setTargetYaw(float yaw)   { this.entityData.set(TARGET_YAW, yaw); }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               300.0)
                .add(Attributes.MOVEMENT_SPEED,             0.0)
                .add(Attributes.ATTACK_DAMAGE,             14.0)
                .add(Attributes.FOLLOW_RANGE,              40.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE,   5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,       1.0);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0 || splitDelayTick > 0) return true;
        cancelTurn();
        this.triggerAnim("events_controller", "attack_right");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    private void tickAttack() {
        if (attackDelayTick <= 0) return;
        attackDelayTick++;
        if (attackDelayTick == ATTACK_HIT_TICK) {
            if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                    && this.distanceTo(pendingAttackTarget)
                        <= this.getBbWidth() * 0.5F + pendingAttackTarget.getBbWidth() + 4.0F) {
                super.doHurtTarget(pendingAttackTarget);
            }
            pendingAttackTarget = null;
        }
        if (attackDelayTick >= ATTACK_TOTAL_TICKS) {
            attackDelayTick = 0;
        }
    }

    private void tickSplit() {
        if (splitDelayTick <= 0) return;
        splitDelayTick++;
        if (splitDelayTick == SPLIT_FIRE_TICK) {
            if (splitTarget != null && splitTarget.isAlive()) {
                fireSplit(splitTarget);
            }
            splitTarget = null;
        }
        if (splitDelayTick >= SPLIT_TOTAL_TICKS) {
            splitDelayTick = 0;
        }
    }

    private void fireSplit(LivingEntity target) {
        Vec3 muzzle   = headMuzzlePosition(target);
        Vec3 aimPoint = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 baseDir  = aimPoint.subtract(muzzle);

        int half = SPLIT_PROJECTILES / 2;
        for (int i = -half; i <= half; i++) {
            Vec3 dir = rotateAroundY(baseDir, Math.toRadians(i * SPLIT_SPREAD_DEG)).normalize();
            PoisonSplitProjectile proj = new PoisonSplitProjectile(this.level(), this);
            proj.setPos(muzzle.x, muzzle.y, muzzle.z);
            proj.shoot(dir.x, dir.y, dir.z, SPLIT_VELOCITY, 2.0F);
            this.level().addFreshEntity(proj);
        }

        this.playSound(SoundEvents.LLAMA_SPIT, 1.3F, 0.6F + this.random.nextFloat() * 0.2F);
    }

    private Vec3 headMuzzlePosition(LivingEntity target) {
        Vec3 base = this.position();
        Vec3 flat = target.position().subtract(base);
        flat = new Vec3(flat.x, 0.0, flat.z);
        if (flat.lengthSqr() < 1.0e-4) {
            float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
            flat = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        }
        flat = flat.normalize();
        return base.add(flat.scale(HEAD_FORWARD)).add(0.0, HEAD_HEIGHT, 0.0);
    }

    private static Vec3 rotateAroundY(Vec3 v, double rad) {
        double cos = Math.cos(rad), sin = Math.sin(rad);
        return new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos);
    }

    // Tick

    @Override
    public void tick() {
        if (this.deathTime > 0 && deathAnimTick < DEATH_ANIM_TICKS) {
            this.deathTime = Math.min(this.deathTime, 18);
        }
        super.tick();
        if (this.deathTime > 0) deathAnimTick++;

        if (!this.level().isClientSide) {
            Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(0.0, dm.y, 0.0);

            this.tickTurnStateMachine();

            // this.tickAttack();
            // this.tickSplit();
        }

        this.applyTurnRotation();

        if (!this.level().isClientSide) {
            this.tickHeadOrientation();
        }

        this.tickParts();
    }

    private float yawError() {
        return Mth.wrapDegrees(this.getTargetYaw() - this.getYRot());
    }

    private void cancelTurn() {
        if (getTurnState() != TURN_NONE) setTurnState(TURN_NONE);
        turnPhaseTick = 0;
    }

    private LivingEntity getLookTarget() {
        LivingEntity t = this.getTarget();
        if (t != null && t.isAlive()) return t;
        return this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), LOOK_RANGE, false);
    }

    private void tickTurnStateMachine() {
        LivingEntity target = this.getLookTarget();
        int state = getTurnState();

        if (target == null || !target.isAlive()) {
            cancelTurn();
            return;
        }

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz > 1.0e-6) {
            setTargetYaw((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
        }
        float err = yawError();

        switch (state) {
            case TURN_NONE -> {
                if (Math.abs(err) > TURN_ANIM_THRESHOLD) {
                    setTurnState(err > 0 ? R_START : L_START);
                    turnPhaseTick = 0;
                }
            }
            case R_START, L_START -> {
                if (++turnPhaseTick >= TURN_START_TICKS) {
                    setTurnState(state == R_START ? R_TURN : L_TURN);
                    turnPhaseTick = 0;
                }
            }
            case R_TURN, L_TURN -> {
                if (Math.abs(err) <= TURN_ANIM_THRESHOLD) {
                    setTurnState(state == R_TURN ? R_END : L_END);
                    turnPhaseTick = 0;
                } else {
                    int wanted = err > 0 ? R_TURN : L_TURN;
                    if (state != wanted) setTurnState(wanted);
                }
            }
            case R_END, L_END -> {
                if (++turnPhaseTick >= TURN_END_TICKS) {
                    cancelTurn();
                }
            }
        }
    }

    private void applyTurnRotation() {
        int state = getTurnState();
        boolean windup = (state == R_START || state == L_START);
        if (!windup) {
            float err = yawError();
            float abs = Math.abs(err);
            float speed = abs > TURN_ANIM_THRESHOLD ? TURN_SPEED_FAST
                        : abs > TURN_SILENT_MIN     ? TURN_SPEED_SLOW
                        : 0.0f;
            if (speed > 0.0f) {
                this.setYRot(Mth.wrapDegrees(this.getYRot() + Mth.clamp(err, -speed, speed)));
            }
        }
        this.yBodyRot = this.getYRot();
        this.yBodyRotO = this.yRotO;
    }

    private void tickHeadOrientation() {
        LivingEntity target = this.getLookTarget();
        if (target == null || !target.isAlive()) {
            this.yHeadRot = this.yBodyRot;
            this.setXRot(0.0F);
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getEyeY() - (this.getY() + HEAD_TRACK_HEIGHT);

        float desiredYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float yawOff = Mth.clamp(Mth.wrapDegrees(desiredYaw - this.yBodyRot), -HEAD_TRACK_CLAMP, HEAD_TRACK_CLAMP);
        this.yHeadRot = Mth.wrapDegrees(this.yBodyRot + yawOff);
        this.setXRot(Mth.clamp((float) (-(Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG)), -HEAD_TRACK_CLAMP, HEAD_TRACK_CLAMP));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        if (effect.is(MobEffects.POISON)) return false;
        return super.canBeAffected(effect);
    }

    // Boss bar

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // Death

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        cancelTurn();
        this.triggerAnim("events_controller", "death");
    }

    // Sons

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.WARDEN_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.WARDEN_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.WARDEN_DEATH; }
    @Override protected float getSoundVolume()                    { return 1.4F; }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "base", 3, state -> switch (this.getTurnState()) {
            case R_START -> state.setAndContinue(TURN_R_START);
            case R_TURN  -> state.setAndContinue(TURN_R_LOOP);
            case R_END   -> state.setAndContinue(TURN_R_END);
            case L_START -> state.setAndContinue(TURN_L_START);
            case L_TURN  -> state.setAndContinue(TURN_L_LOOP);
            case L_END   -> state.setAndContinue(TURN_L_END);
            default      -> state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "events_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack_right", ATTACK_ANIM)
                .triggerableAnim("split",        SPLIT_ANIM)
                .triggerableAnim("death",        DEATH_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
