package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.entity.projectile.AcidSplitProjectile;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PurpleWormEntity extends Monster implements GeoEntity {

    private static final int   ATTACK_HIT_TICK    = 15;
    private static final int   ATTACK_SOUND_TICK  = 12;
    private static final int   ATTACK_TOTAL_TICKS = 16;
    private static final float MELEE_REACH        = 4.0f;
    private static final float CLAW_KNOCKBACK     = 3.0f;
    private static final int   DEATH_ANIM_TICKS   = 40;

    // Bite
    private static final int    BITE_TOTAL_TICKS  = 25;
    private static final int    BITE_WINDUP_TICKS = 10;
    private static final double BITE_MIN_RANGE    = 4.0;
    private static final double BITE_MAX_RANGE    = 10.0;
    private static final int   BITE_DAMAGE_START  = 6;
    private static final int   BITE_DAMAGE_END    = 22;
    private static final int   BITE_TRACK_RESUME  = 20;
    private static final float BITE_KNOCKBACK     = 1.0f;
    private static final float BITE_TURN_SPEED    = 8.0f;
    private static final double BITE_CONTACT_PAD  = 0.2;

    // Split
    private static final int    SPLIT_COOLDOWN_TICKS = 30;
    private static final int    SPLIT_FIRE_TICK      = 12;
    private static final int    SPLIT_TOTAL_TICKS    = 25;
    private static final int    SPLIT_PROJECTILES    = 1;

    // Multisplit
    private static final int    MULTISPLIT_COOLDOWN_TICKS = 150;
    private static final int    MULTISPLIT_FIRE_TICK      = 12;
    private static final int    MULTISPLIT_TOTAL_TICKS    = 25;
    private static final float  MULTISPLIT_BASE_SCALE     = 2.0f;
    private static final double MULTISPLIT_MIN_RANGE      = 6.0;
    private static final double MULTISPLIT_MAX_RANGE      = 26.0;
    private static final float  SPLIT_SPREAD_DEG     = 12.0f;
    private static final float  SPLIT_VELOCITY       = 1.8f;
    private static final double SPLIT_DROP_LEAD      = 0.12;

    // Breath
    private static final int    BREATH_TOTAL_TICKS    = 120;
    private static final int    BREATH_START_TICK     = 50;
    private static final int    BREATH_END_TICK       = 100;
    private static final int    BREATH_COOLDOWN_TICKS = 300;
    private static final int    BREATH_ACID_BUBBLES   = 4;
    private static final int[]  BREATH_CLOUD_TICKS    = { 60, 70, 80, 90 };
    private static final double BREATH_CLOUD_RAYCAST  = 20.0;
    private static final float  BREATH_DAMAGE         = 5.0f;
    private static final float  BREATH_RANGE          = 14.0f;
    private static final float  BREATH_CONE_COS       = Mth.cos(35.0f * Mth.DEG_TO_RAD);
    private static final int    BREATH_ACID_TICKS   = 100;
    private static final double BREATH_MIN_RANGE      = 5.0;
    private static final double BREATH_MAX_RANGE      = 16.0;

    // Base Fog
    private static final int    BASE_FOG_INTERVAL  = 3;
    private static final int    BASE_FOG_PARTICLES = 2;
    private static final double BASE_FOG_RADIUS    = 2.6;

    // Summon Claws
    private static final int    SUMMON_TOTAL_TICKS    = 120;
    private static final int    SUMMON_SPAWN_START    = 25;
    private static final double SUMMON_ARM_RADIUS     = 1.0;
    private static final int    SUMMON_ARM_TRIES      = 8;
    private static final int    SUMMON_COOLDOWN_TICKS = 400;
    private static final double SUMMON_MAX_RANGE      = 30.0;

    // Split Volley
    private static final int    SVOLLEY_TOTAL_TICKS   = 65;
    private static final int[]  SVOLLEY_TICKS         = { 38, 48, 58 };
    private static final int    SVOLLEY_COOLDOWN_TICKS = 100;
    private static final double SVOLLEY_MIN_RANGE     = 8.0;
    private static final double SVOLLEY_MAX_RANGE     = 30.0;
    private static final double SVOLLEY_FAR_OFFSET    = 6.0;
    private static final double SVOLLEY_NEAR_OFFSET   = 6.0;
    private static final int    SVOLLEY_FLIGHT_TICKS  = 40;
    private static final float  SVOLLEY_SPREAD_DEG    = 9.0f;
    private static final double PROJ_GRAVITY = 0.03, PROJ_INERTIA = 0.99;

    private static final int TURN_NONE = 0, R_START = 1, R_TURN = 2, R_END = 3, L_START = 4, L_TURN = 5, L_END = 6;

    private static final int ATK_CLAW = 0, ATK_BITE = 1, ATK_SPLIT = 2, ATK_BREATH = 3,
            ATK_MULTISPLIT = 4, ATK_SUMMON = 5, ATK_SVOLLEY = 6;

    // Hide/Reveal
    private static final int HIDE_HIDDEN = 0, HIDE_REVEALING = 1, HIDE_REVEALED = 2, HIDE_HIDING = 3;
    private static final double REVEAL_RANGE      = 20.0;
    private static final int    HIDE_DELAY_TICKS  = 200;
    private static final int    REVEAL_ANIM_TICKS = 10;
    private static final int    HIDE_ANIM_TICKS   = 15;
    private static final float  HIDE_HEAL_PER_TICK = 0.5f;

    private static final EntityDataAccessor<Integer> HIDE_STATE =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.INT);

    private static final float TURN_ANIM_THRESHOLD = 80.0f;
    private static final float TURN_SILENT_MIN     = 45.0f;
    private static final float TURN_SPEED_FAST     = 5.0f;
    private static final float TURN_SPEED_SLOW     = 3.0f;
    private static final float TURN_SPEED_MIN      = 1.0f;
    private static final int   TURN_START_TICKS    = 10;
    private static final int   TURN_END_TICKS      = 20;
    private static final float SPLIT_FACING_MAX    = 35.0f;
    private static final double HEAD_HEIGHT  = 138.667 / 16.0;
    private static final double HEAD_FORWARD = 53.917 / 16.0;

    private static final EntityDataAccessor<Integer> TURN_STATE =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_YAW =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BITE_AIMING =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BITE_ACTIVE =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BREATHING =
            SynchedEntityData.defineId(PurpleWormEntity.class, EntityDataSerializers.BOOLEAN);

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
    private Vec3   breathHeadForward   = null;
    private Vec3   miniHeadOffsetL     = null;
    private Vec3   miniHeadOffsetR     = null;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;
    private int    deathAnimTick       = 0;
    private int    turnPhaseTick       = 0;

    private int biteDelayTick = 0;
    private final Set<UUID> biteHitTargets = new HashSet<>();

    private int hideAnimTimer = 0;
    private int awayTimer      = 0;
    private Double anchorX = null, anchorZ = null;

    private int phase          = 1;

    private static final float HEAD_EASE = 0.35f;
    private float headYaw, headYawO, headPitch, headPitchO;

    private LivingEntity splitTarget    = null;
    private int          splitDelayTick = 0;
    private int          splitCooldown  = 0;

    private int breathDelayTick = 0;
    private int breathCooldown  = 0;

    private LivingEntity multiSplitTarget    = null;
    private int          multiSplitDelayTick = 0;
    private int          multiSplitCooldown  = 0;

    private LivingEntity summonTarget    = null;
    private int          summonDelayTick = 0;
    private int          summonCooldown  = 0;

    private LivingEntity sVolleyTarget    = null;
    private Vec3         sVolleyLockPos   = null;
    private int          sVolleyDelayTick = 0;
    private int          sVolleyCooldown  = 0;

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK_ANIM      = RawAnimation.begin().then("attack_claw_right", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_LEFT_ANIM = RawAnimation.begin().then("attack_claw_left", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SPLIT_ANIM  = RawAnimation.begin().then("attack_split", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation MULTISPLIT_ANIM = RawAnimation.begin().then("attack_multisplit", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation BITE_ANIM    = RawAnimation.begin().then("attack_bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation BREATH_ANIM  = RawAnimation.begin().then("attack_breath", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SUMMON_ANIM  = RawAnimation.begin().then("attack_summon_claws", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SVOLLEY_ANIM = RawAnimation.begin().then("attack_split_volley", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation DEATH_ANIM  = RawAnimation.begin().then("death", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation HIDE_ANIM    = RawAnimation.begin().then("hide", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation REVEAL_ANIM  = RawAnimation.begin().then("reveal", Animation.LoopType.HOLD_ON_LAST_FRAME);

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

    public void setBreathHeadForward(Vec3 forward) {
        this.breathHeadForward = forward;
    }

    public void setMiniHeadOffsets(Vec3 left, Vec3 right) {
        this.miniHeadOffsetL = left;
        this.miniHeadOffsetR = right;
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
        builder.define(BITE_AIMING, false);
        builder.define(BITE_ACTIVE, false);
        builder.define(IS_BREATHING, false);
        builder.define(HIDE_STATE, HIDE_HIDDEN);
    }

    public boolean isBreathing()          { return this.entityData.get(IS_BREATHING); }
    private void   setBreathing(boolean b){ this.entityData.set(IS_BREATHING, b); }

    public int  getHideState()          { return this.entityData.get(HIDE_STATE); }
    public void setHideState(int state) { this.entityData.set(HIDE_STATE, state); }
    public boolean isHidden()           { return getHideState() == HIDE_HIDDEN; }

    public int   getTurnState()            { return this.entityData.get(TURN_STATE); }
    public void  setTurnState(int state)   { this.entityData.set(TURN_STATE, state); }
    public float getTargetYaw()            { return this.entityData.get(TARGET_YAW); }
    public void  setTargetYaw(float yaw)   { this.entityData.set(TARGET_YAW, yaw); }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               300.0)
                .add(Attributes.ARMOR,               20.0)
                .add(Attributes.MOVEMENT_SPEED,             0.0)
                .add(Attributes.ATTACK_DAMAGE,             16.0)
                .add(Attributes.FOLLOW_RANGE,              100.0)
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
        if (attackDelayTick > 0 || splitDelayTick > 0 || biteDelayTick > 0) return true;
        startClaw(target);
        return true;
    }

    // Attack

    private void startClaw(Entity target) {
        cancelTurn();
        this.triggerAnim("events_controller", this.random.nextBoolean() ? "attack_right" : "attack_left");
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
    }

    private void startBite() {
        cancelTurn();
        this.triggerAnim("events_controller", "bite");
        this.biteHitTargets.clear();
        this.biteDelayTick = 1;
        this.entityData.set(BITE_AIMING, true);
        this.entityData.set(BITE_ACTIVE, true);
    }

    private void startSplit(LivingEntity target) {
        cancelTurn();
        this.triggerAnim("events_controller", "split");
        this.splitTarget    = target;
        this.splitDelayTick = 1;
    }

    private void startMultiSplit(LivingEntity target) {
        cancelTurn();
        this.triggerAnim("events_controller", "multisplit");
        this.multiSplitTarget    = target;
        this.multiSplitDelayTick = 1;
    }

    private void startSplitVolley(LivingEntity target) {
        cancelTurn();
        this.triggerAnim("events_controller", "split_volley");
        this.sVolleyTarget    = target;
        this.sVolleyLockPos   = null;
        this.sVolleyDelayTick = 1;
    }

    private void tickSplitVolley() {
        if (sVolleyDelayTick <= 0) return;
        sVolleyDelayTick++;
        if (sVolleyDelayTick == 2) {
            this.playSound(SoundsRegistry.PURPLE_WORM_SPLIT_VOLLEY.get(), 2.6F, 1.0F);
        }
        for (int i = 0; i < SVOLLEY_TICKS.length; i++) {
            if (sVolleyDelayTick != SVOLLEY_TICKS[i]) continue;
            if (i == 0 && sVolleyTarget != null && sVolleyTarget.isAlive()) {
                sVolleyLockPos = sVolleyTarget.position();
            }
            if (sVolleyLockPos != null) fireVolleySalvo(i);
        }
        if (sVolleyDelayTick >= SVOLLEY_TOTAL_TICKS) {
            sVolleyDelayTick = 0;
            sVolleyTarget    = null;
            sVolleyLockPos   = null;
            this.sVolleyCooldown = SVOLLEY_COOLDOWN_TICKS;
        }
    }

    private void fireVolleySalvo(int index) {
        Vec3 muzzle = breathMuzzle();
        double hx = sVolleyLockPos.x - this.getX();
        double hz = sVolleyLockPos.z - this.getZ();
        double hlen = Math.sqrt(hx * hx + hz * hz);
        Vec3 dir = hlen > 1.0e-4 ? new Vec3(hx / hlen, 0.0, hz / hlen) : new Vec3(0, 0, 1);

        Vec3 landing = switch (index) {
            case 0  -> sVolleyLockPos.add(dir.scale(SVOLLEY_FAR_OFFSET));
            case 2  -> sVolleyLockPos.subtract(dir.scale(SVOLLEY_NEAR_OFFSET));
            default -> sVolleyLockPos;
        };

        Vec3 v = ballisticVelocity(muzzle, landing, SVOLLEY_FLIGHT_TICKS);
        int half = 1;
        for (int j = -half; j <= half; j++) {
            Vec3 vj = rotateAroundY(v, Math.toRadians(j * SVOLLEY_SPREAD_DEG));
            AcidSplitProjectile proj = new AcidSplitProjectile(this.level(), this);
            proj.setPos(muzzle.x, muzzle.y, muzzle.z);
            proj.setDeltaMovement(vj);
            aimRotationFromVelocity(proj, vj);
            this.level().addFreshEntity(proj);
        }
    }

    private static Vec3 ballisticVelocity(Vec3 muzzle, Vec3 landing, int n) {
        double f = PROJ_INERTIA, g = PROJ_GRAVITY;
        double s = (1.0 - Math.pow(f, n)) / (1.0 - f);
        double dx = landing.x - muzzle.x;
        double dy = landing.y - muzzle.y;
        double dz = landing.z - muzzle.z;
        double vx = dx / s;
        double vz = dz / s;
        double vy = (dy + g * (n - s) / (1.0 - f)) / s;
        return new Vec3(vx, vy, vz);
    }

    private static void aimRotationFromVelocity(AcidSplitProjectile proj, Vec3 v) {
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        proj.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        proj.setXRot((float) (Mth.atan2(v.y, horiz) * Mth.RAD_TO_DEG));
        proj.yRotO = proj.getYRot();
        proj.xRotO = proj.getXRot();
    }

    private void startSummonClaw(LivingEntity target) {
        cancelTurn();
        this.triggerAnim("events_controller", "summon_claws");
        this.summonTarget    = target;
        this.summonDelayTick = 1;
    }

    private void tickSummonClaw() {
        if (summonDelayTick <= 0) return;
        summonDelayTick++;
        if (summonDelayTick == SUMMON_SPAWN_START && summonTarget != null && summonTarget.isAlive()) {
            int count = (phase == 2) ? 2 : 1;
            for (int k = 0; k < count; k++) spawnArm(summonTarget);
        }
        if (summonDelayTick >= SUMMON_TOTAL_TICKS) {
            summonDelayTick = 0;
            summonTarget    = null;
            this.summonCooldown = SUMMON_COOLDOWN_TICKS;
        }
    }

    private Double findGroundY(net.minecraft.server.level.ServerLevel server, double x, double z, double startY) {
        Vec3 from = new Vec3(x, startY, z);
        Vec3 to   = new Vec3(x, server.getMinBuildHeight(), z);
        net.minecraft.world.phys.BlockHitResult hit = server.clip(new net.minecraft.world.level.ClipContext(
                from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK ? hit.getLocation().y : null;
    }

    private void spawnArm(LivingEntity target) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel server)) return;

        double ax = target.getX(), az = target.getZ();
        Double gy = null;

        for (int attempt = 0; attempt < SUMMON_ARM_TRIES; attempt++) {
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            double rad = SUMMON_ARM_RADIUS * (attempt + 1) / (double) SUMMON_ARM_TRIES;
            double tx = target.getX() + Math.cos(ang) * rad;
            double tz = target.getZ() + Math.sin(ang) * rad;
            Double y = findGroundY(server, tx, tz, target.getY() + 2.0);
            if (y == null) continue;

            BlockPos above = BlockPos.containing(tx, y + 0.5, tz);
            if (!server.getBlockState(above).getCollisionShape(server, above).isEmpty()) continue;
            ax = tx; az = tz; gy = y;
            break;
        }

        if (gy == null) {
            gy = findGroundY(server, target.getX(), target.getZ(), target.getY() + 2.0);
            ax = target.getX(); az = target.getZ();
        }
        if (gy == null) return;

        PurpleWormArmEntity arm = new PurpleWormArmEntity(
                EntityRegistry.PURPLE_WORM_ARM.get(), this.level());
        arm.setPos(ax, gy, az);
        float yaw = (float) (Mth.atan2(target.getZ() - az, target.getX() - ax) * Mth.RAD_TO_DEG) - 90.0F;
        arm.setYRot(yaw);
        arm.yBodyRot = arm.yHeadRot = yaw;
        server.addFreshEntity(arm);
    }

    private void startBreath() {
        cancelTurn();
        this.triggerAnim("events_controller", "breath");
        this.breathDelayTick = 1;
    }

    private void tickBreath() {
        if (breathDelayTick <= 0) return;
        breathDelayTick++;
        if (breathDelayTick == 2) {
            this.playSound(SoundsRegistry.PURPLE_WORM_BREATH.get(), 2.0F, 1.0F);
        }
        boolean active = breathDelayTick >= BREATH_START_TICK && breathDelayTick <= BREATH_END_TICK;
        setBreathing(active);
        if (active) {
            applyBreathDamage();
            for (int t : BREATH_CLOUD_TICKS) {
                if (breathDelayTick == t) { spawnAcidCloud(); break; }
            }
        }
        if (breathDelayTick >= BREATH_TOTAL_TICKS) {
            breathDelayTick = 0;
            setBreathing(false);
            breathCooldown = BREATH_COOLDOWN_TICKS;
        }
    }

    private Vec3 breathMuzzle() {
        Vec3[] off = this.animatedPartOffsets;
        int hi = PART_COUNT - 1;
        if (off != null && off.length > hi && off[hi] != null) {
            return this.position().add(off[hi]);
        }
        float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        Vec3 flat = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        return this.position().add(flat.scale(HEAD_FORWARD)).add(0.0, HEAD_HEIGHT, 0.0);
    }

    private Vec3 breathLook(Vec3 muzzle) {
        if (this.breathHeadForward != null && this.breathHeadForward.lengthSqr() > 1.0e-6) {
            return this.breathHeadForward.normalize();
        }
        LivingEntity t = this.getLookTarget();
        if (t != null && t.isAlive()) {
            Vec3 look = new Vec3(t.getX() - muzzle.x,
                    t.getY() + t.getBbHeight() * 0.5 - muzzle.y,
                    t.getZ() - muzzle.z);
            if (look.lengthSqr() > 1.0e-4) return look.normalize();
        }
        float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
    }

    private void spawnAcidCloud() {
        if (!(this.level() instanceof ServerLevel server)) return;
        Vec3 muzzle = breathMuzzle();
        Vec3 dir    = breathLook(muzzle);
        Vec3 far    = muzzle.add(dir.scale(BREATH_CLOUD_RAYCAST));

        BlockHitResult hit = server.clip(new ClipContext(
                muzzle, far, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 pos = hit.getType() == HitResult.Type.BLOCK
                ? hit.getLocation()
                : muzzle.add(dir.scale(2.0));

        AcidCloudEntity cloud = new AcidCloudEntity(
                EntityRegistry.ACID_CLOUD.get(), this.level());
        cloud.setPos(pos.x, pos.y, pos.z);
        this.level().addFreshEntity(cloud);
    }

    private void applyBreathDamage() {
        Vec3 muzzle = breathMuzzle();
        Vec3 look   = breathLook(muzzle);
        double rangeSq = BREATH_RANGE * BREATH_RANGE;

        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(BREATH_RANGE))) {
            if (victim == this) continue;
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            Vec3 to = new Vec3(victim.getX() - muzzle.x,
                    victim.getY() + victim.getBbHeight() * 0.5 - muzzle.y,
                    victim.getZ() - muzzle.z);
            if (to.lengthSqr() > rangeSq) continue;
            if ((float) look.dot(to.normalize()) < BREATH_CONE_COS) continue;
            victim.hurt(this.damageSources().mobAttack(this), BREATH_DAMAGE);
            if (!victim.isDeadOrDying()) {
                victim.addEffect(new MobEffectInstance(MobEffectsRegistry.ACID, BREATH_ACID_TICKS, 0, false, false, true));
            }
        }
    }

    private void spawnBaseFog() {
        for (int i = 0; i < BASE_FOG_PARTICLES; i++) {
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            double rad = BASE_FOG_RADIUS * Math.sqrt(this.random.nextDouble());
            this.level().addParticle(
                    ParticlesRegistry.ACID_CLOUD.get(),
                    this.getX() + Math.cos(ang) * rad,
                    this.getY() + this.random.nextDouble() * 0.25,
                    this.getZ() + Math.sin(ang) * rad,
                    0.0, 0.005 + this.random.nextDouble() * 0.01, 0.0);
        }
    }

    private void spawnBreathParticles() {
        Vec3 muzzle = breathMuzzle();
        Vec3 look   = breathLook(muzzle).normalize();
        Vec3 mouth  = muzzle.add(look.scale(1.0));
        for (int i = 0; i < 10; i++) {
            double a = 0.5;
            Vec3 spread = new Vec3(
                    this.random.nextDouble() * 2 * a - a,
                    this.random.nextDouble() * 2 * a - a,
                    this.random.nextDouble() * 2 * a - a);
            Vec3 vel = look.scale(2.5).add(spread).normalize().scale(0.3 + this.random.nextDouble() * 0.25);
            this.level().addParticle(ParticlesRegistry.ACID_CLOUD.get(),
                    mouth.x + spread.x * 0.4, mouth.y + spread.y * 0.4, mouth.z + spread.z * 0.4,
                    vel.x, vel.y, vel.z);
        }
        for (int i = 0; i < BREATH_ACID_BUBBLES; i++) {
            double a = 0.35;
            Vec3 spread = new Vec3(
                    this.random.nextDouble() * 2 * a - a,
                    this.random.nextDouble() * 2 * a - a,
                    this.random.nextDouble() * 2 * a - a);
            Vec3 vel = look.scale(3.0).add(spread).normalize().scale(0.55 + this.random.nextDouble() * 0.35);
            this.level().addParticle(net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry.ACID_BUBBLE.get(),
                    mouth.x + spread.x * 0.3, mouth.y + spread.y * 0.3, mouth.z + spread.z * 0.3,
                    vel.x, vel.y, vel.z);
        }
    }

    private double meleeReach(Entity target) {
        return this.getBbWidth() * 0.5 + target.getBbWidth() + MELEE_REACH;
    }

    private void tickAttack() {
        if (attackDelayTick <= 0) return;
        attackDelayTick++;
        if (attackDelayTick == ATTACK_SOUND_TICK) {
            this.playSound(SoundsRegistry.PURPLE_WORM_CLAW.get(), 1.2F,
                    0.95F + this.random.nextFloat() * 0.1F);
        }
        if (attackDelayTick == ATTACK_HIT_TICK) {
            if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                    && this.distanceTo(pendingAttackTarget) <= meleeReach(pendingAttackTarget)) {
                if (pendingAttackTarget instanceof LivingEntity living) {
                    living.hurt(this.damageSources().mobAttack(this),
                            (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    double dx = living.getX() - this.getX();
                    double dz = living.getZ() - this.getZ();
                    double len = Math.sqrt(dx * dx + dz * dz);
                    if (len > 0) {
                        living.knockback(CLAW_KNOCKBACK, -dx / len, -dz / len);
                    }
                } else {
                    super.doHurtTarget(pendingAttackTarget);
                }
            }
            pendingAttackTarget = null;
        }
        if (attackDelayTick >= ATTACK_TOTAL_TICKS) {
            attackDelayTick = 0;
        }
    }

    private void tickBite() {
        if (biteDelayTick <= 0) return;
        biteDelayTick++;
        if (biteDelayTick > BITE_WINDUP_TICKS && this.entityData.get(BITE_AIMING)) {
            this.entityData.set(BITE_AIMING, false);
        }
        if (biteDelayTick == 2) {
            this.playSound(SoundsRegistry.PURPLE_WORM_BITE.get(), 1.3F,
                    0.95F + this.random.nextFloat() * 0.1F);
        }
        if (biteDelayTick >= BITE_DAMAGE_START && biteDelayTick <= BITE_DAMAGE_END) {
            applyBiteContactDamage();
        }
        if (biteDelayTick == BITE_TRACK_RESUME) {
            this.entityData.set(BITE_ACTIVE, false);
        }
        if (biteDelayTick >= BITE_TOTAL_TICKS) {
            biteDelayTick = 0;
            biteHitTargets.clear();
            this.entityData.set(BITE_AIMING, false);
            this.entityData.set(BITE_ACTIVE, false);
        }
    }

    private void applyBiteContactDamage() {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (PurpleWormPart part : this.subParts) {
            AABB box = part.getBoundingBox().inflate(BITE_CONTACT_PAD);
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != this && e.isAlive())) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                if (!biteHitTargets.add(victim.getUUID())) continue;
                victim.hurt(this.damageSources().mobAttack(this), dmg);
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) victim.knockback(BITE_KNOCKBACK, -dx / len, -dz / len);
            }
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
            this.splitCooldown = SPLIT_COOLDOWN_TICKS;
        }
    }

    private void fireSplit(LivingEntity target) {
        fireProjectilesFrom(headMuzzlePosition(target), target, SPLIT_PROJECTILES, 1.0f);
        this.playSound(SoundsRegistry.PURPLE_WORM_SPLIT.get(), 2.6F,
                0.95F + this.random.nextFloat() * 0.1F);
    }

    private void tickMultiSplit() {
        if (multiSplitDelayTick <= 0) return;
        multiSplitDelayTick++;
        if (multiSplitDelayTick == MULTISPLIT_FIRE_TICK) {
            if (multiSplitTarget != null && multiSplitTarget.isAlive()) {
                fireMultiSplit(multiSplitTarget);
            }
            multiSplitTarget = null;
        }
        if (multiSplitDelayTick >= MULTISPLIT_TOTAL_TICKS) {
            multiSplitDelayTick = 0;
            this.multiSplitCooldown = MULTISPLIT_COOLDOWN_TICKS;
        }
    }

    private void fireMultiSplit(LivingEntity target) {
        fireProjectilesFrom(headMuzzlePosition(target),      target, 1, MULTISPLIT_BASE_SCALE);
        fireProjectilesFrom(miniHeadMuzzle(miniHeadOffsetL), target, 1, 1.0f);
        fireProjectilesFrom(miniHeadMuzzle(miniHeadOffsetR), target, 1, 1.0f);
        this.playSound(SoundsRegistry.PURPLE_WORM_SPLIT.get(), 2.8F,
                0.9F + this.random.nextFloat() * 0.1F);
    }

    private Vec3 miniHeadMuzzle(Vec3 offset) {
        if (offset != null) return this.position().add(offset);
        float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        Vec3 flat = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        return this.position().add(flat.scale(HEAD_FORWARD * 0.6)).add(0.0, HEAD_HEIGHT * 0.6, 0.0);
    }

    private void fireProjectilesFrom(Vec3 muzzle, LivingEntity target, int count, float scale) {
        double horiz = Math.sqrt(
                Math.pow(target.getX() - muzzle.x, 2) + Math.pow(target.getZ() - muzzle.z, 2));
        Vec3 aimPoint = target.position().add(0.0, target.getBbHeight() * 0.5 + horiz * SPLIT_DROP_LEAD, 0.0);
        Vec3 baseDir  = aimPoint.subtract(muzzle);

        int half = count / 2;
        for (int i = -half; i <= half; i++) {
            Vec3 dir = rotateAroundY(baseDir, Math.toRadians(i * SPLIT_SPREAD_DEG)).normalize();
            AcidSplitProjectile proj = new AcidSplitProjectile(this.level(), this);
            proj.setScale(scale);
            proj.setPos(muzzle.x, muzzle.y, muzzle.z);
            proj.shoot(dir.x, dir.y, dir.z, SPLIT_VELOCITY, 2.0F);
            this.level().addFreshEntity(proj);
        }
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

            // Verrouillage dur de la position horizontale. super.tick() a deja applique le
            // deplacement : annuler la velocite ne suffit donc pas (une impulsion le decalerait
            // d'un tick avant d'etre neutralisee). On le replace sur son point d'ancrage.
            // Y reste libre : gravite et flottaison continuent de fonctionner.
            if (this.anchorX == null) {
                this.anchorX = this.getX();
                this.anchorZ = this.getZ();
            } else if (this.getX() != this.anchorX || this.getZ() != this.anchorZ) {
                this.setPos(this.anchorX, this.getY(), this.anchorZ);
            }

            this.tickTurnStateMachine();

            this.tickAttack();
            this.tickBite();
            this.tickSplit();
            this.tickMultiSplit();
            this.tickSplitVolley();
            this.tickSummonClaw();
            this.tickBreath();
        } else {
            if (this.isBreathing()) this.spawnBreathParticles();
            if (!this.isHidden() && this.tickCount % BASE_FOG_INTERVAL == 0) {
                this.spawnBaseFog();
            }
        }

        this.applyTurnRotation();
        this.tickHeadTracking();

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

        if (biteDelayTick > 0) {
            if (state != TURN_NONE) cancelTurn();
            return;
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
        if (this.entityData.get(BITE_AIMING)) {
            float err = yawError();
            this.setYRot(Mth.wrapDegrees(this.getYRot() + Mth.clamp(err, -BITE_TURN_SPEED, BITE_TURN_SPEED)));
            this.yBodyRot = this.getYRot();
            this.yBodyRotO = this.yRotO;
            return;
        }

        int state = getTurnState();
        boolean windup = (state == R_START || state == L_START);
        if (!windup) {
            float err = yawError();
            float abs = Math.abs(err);
            float speed = abs > TURN_ANIM_THRESHOLD ? TURN_SPEED_FAST
                        : abs > TURN_SILENT_MIN     ? TURN_SPEED_SLOW
                        :                             TURN_SPEED_MIN;
            this.setYRot(Mth.wrapDegrees(this.getYRot() + Mth.clamp(err, -speed, speed)));
        }
        this.yBodyRot = this.getYRot();
        this.yBodyRotO = this.yRotO;
    }

    private void tickHeadTracking() {
        this.headYawO   = this.headYaw;
        this.headPitchO = this.headPitch;

        float wantYaw = 0.0f, wantPitch = 0.0f;

        LivingEntity target = this.getLookTarget();
        if (target != null && target.isAlive()) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double horiz = Math.sqrt(dx * dx + dz * dz);
            double dy = target.getEyeY() - (this.getY() + HEAD_TRACK_HEIGHT);

            float desiredYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
            wantYaw   = -Mth.clamp(Mth.wrapDegrees(desiredYaw - this.yBodyRot), -HEAD_TRACK_CLAMP, HEAD_TRACK_CLAMP);
            wantPitch =  Mth.clamp((float) (Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG), -HEAD_TRACK_CLAMP, HEAD_TRACK_CLAMP);
        }
        this.headYaw   += (wantYaw   - this.headYaw)   * HEAD_EASE;
        this.headPitch += (wantPitch - this.headPitch) * HEAD_EASE;
    }

    public float getHeadYawOffset(float partialTick) {
        return Mth.lerp(partialTick, this.headYawO, this.headYaw);
    }

    public float getHeadPitchOffset(float partialTick) {
        return Mth.lerp(partialTick, this.headPitchO, this.headPitch);
    }

    private boolean playerWithinRevealRange() {
        return this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), REVEAL_RANGE, false) != null;
    }

    private void tickHideState() {
        int st = getHideState();
        boolean near = playerWithinRevealRange();
        switch (st) {
            case HIDE_HIDDEN -> {
                if (this.getHealth() < this.getMaxHealth()) this.heal(HIDE_HEAL_PER_TICK);
                if (near) {
                    setHideState(HIDE_REVEALING);
                    hideAnimTimer = 0;
                    this.playSound(SoundsRegistry.PURPLE_WORM_EMERGE.get(), 2.0F, 1.0F);
                }
            }
            case HIDE_REVEALING -> {
                if (++hideAnimTimer >= REVEAL_ANIM_TICKS) {
                    setHideState(HIDE_REVEALED);
                }
            }
            case HIDE_REVEALED -> {
                if (near) {
                    awayTimer = 0;
                } else if (++awayTimer >= HIDE_DELAY_TICKS) {
                    setHideState(HIDE_HIDING);
                    hideAnimTimer = 0;
                    awayTimer = 0;
                    cancelTurn();
                    this.playSound(SoundsRegistry.PURPLE_WORM_HIDE.get(), 1.8F, 1.0F);
                }
            }
            case HIDE_HIDING -> {
                if (++hideAnimTimer >= HIDE_ANIM_TICKS) {
                    setHideState(HIDE_HIDDEN);
                }
            }
        }
        this.bossEvent.setVisible(getHideState() != HIDE_HIDDEN);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        this.tickHideState();
        if (getHideState() != HIDE_REVEALED) return;

        if (splitCooldown      > 0) splitCooldown--;
        if (breathCooldown     > 0) breathCooldown--;
        if (multiSplitCooldown > 0) multiSplitCooldown--;
        if (summonCooldown     > 0) summonCooldown--;
        if (sVolleyCooldown    > 0) sVolleyCooldown--;

        if (phase == 2 && this.getHealth() > this.getMaxHealth() * 0.5f) phase = 1;

        boolean idle = attackDelayTick == 0 && biteDelayTick == 0 && splitDelayTick == 0
                && breathDelayTick == 0 && multiSplitDelayTick == 0 && summonDelayTick == 0
                && sVolleyDelayTick == 0 && getTurnState() == TURN_NONE;

        if (phase == 1 && this.getHealth() <= this.getMaxHealth() * 0.5f && idle) {
            this.startBreath();
            this.phase = 2;
            return;
        }

        if (idle) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double dist = this.distanceTo(target);
                boolean facing = Math.abs(yawError()) <= SPLIT_FACING_MAX;
                boolean los    = this.hasLineOfSight(target);
                boolean p2     = (phase == 2);

                boolean canClaw   = dist <= meleeReach(target);
                boolean canBite   = dist >= BITE_MIN_RANGE && dist <= BITE_MAX_RANGE;
                boolean canSummon = summonCooldown <= 0 && dist <= SUMMON_MAX_RANGE;
                boolean canSplit  = !p2 && splitCooldown <= 0 && los && facing;
                boolean canMulti  =  p2 && multiSplitCooldown <= 0 && los && facing
                        && dist >= MULTISPLIT_MIN_RANGE && dist <= MULTISPLIT_MAX_RANGE;
                boolean canVolley =  p2 && sVolleyCooldown <= 0 && facing
                        && dist >= SVOLLEY_MIN_RANGE && dist <= SVOLLEY_MAX_RANGE;
                boolean canBreath =  p2 && breathCooldown <= 0 && los && facing
                        && dist >= BREATH_MIN_RANGE && dist <= BREATH_MAX_RANGE;

                int[] options = new int[7];
                int n = 0;
                if (canClaw)   options[n++] = ATK_CLAW;
                if (canBite)   options[n++] = ATK_BITE;
                if (canSummon) options[n++] = ATK_SUMMON;
                if (canSplit)  options[n++] = ATK_SPLIT;
                if (canMulti)  options[n++] = ATK_MULTISPLIT;
                if (canVolley) options[n++] = ATK_SVOLLEY;
                if (canBreath) options[n++] = ATK_BREATH;

                if (n > 0) {
                    switch (options[this.random.nextInt(n)]) {
                        case ATK_CLAW       -> this.startClaw(target);
                        case ATK_BITE       -> this.startBite();
                        case ATK_SPLIT      -> this.startSplit(target);
                        case ATK_BREATH     -> this.startBreath();
                        case ATK_MULTISPLIT -> this.startMultiSplit(target);
                        case ATK_SUMMON     -> this.startSummonClaw(target);
                        case ATK_SVOLLEY    -> this.startSplitVolley(target);
                    }
                }
            }
        }
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        if (effect.is(MobEffectsRegistry.ACID)) return false;
        return super.canBeAffected(effect);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (isHidden() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.5f;
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HideState", getHideState());
        tag.putInt("Phase", this.phase);
        if (this.anchorX != null) {
            tag.putDouble("AnchorX", this.anchorX);
            tag.putDouble("AnchorZ", this.anchorZ);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HideState")) setHideState(tag.getInt("HideState"));
        if (tag.contains("Phase"))     this.phase = tag.getInt("Phase");
        if (tag.contains("AnchorX")) {
            this.anchorX = tag.getDouble("AnchorX");
            this.anchorZ = tag.getDouble("AnchorZ");
        }
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

    // --- Ancrage : le ver est enracine, RIEN ne doit le deplacer de sa position ---

    /** Aucun recul, quelle qu'en soit la source (l'attribut seul ne couvre pas tous les appels). */
    @Override
    public void knockback(double strength, double x, double z) {
        // no-op
    }

    /** Ignore les poussees de collision (autres entites, mobs qui s'appuient dessus). */
    @Override
    public void push(double x, double y, double z) {
        // no-op
    }

    /** Les courants d'eau / lave ne l'emportent pas. */
    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    /** Le souffle des explosions ne l'ejecte pas. */
    @Override
    public boolean ignoreExplosion(@NotNull net.minecraft.world.level.Explosion explosion) {
        return true;
    }

    // Death

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        cancelTurn();
        this.triggerAnim("events_controller", "death");
    }

    // Sons

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.PURPLE_WORM_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.PURPLE_WORM_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.PURPLE_WORM_DEATH.get(); }
    @Override protected float getSoundVolume()                    { return 1.4F; }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "base", 3, state -> {
            switch (this.getHideState()) {
                case HIDE_HIDDEN, HIDE_HIDING -> { return state.setAndContinue(HIDE_ANIM); }
                case HIDE_REVEALING           -> { return state.setAndContinue(REVEAL_ANIM); }
                default -> {}
            }
            return switch (this.getTurnState()) {
                case R_START -> state.setAndContinue(TURN_R_START);
                case R_TURN  -> state.setAndContinue(TURN_R_LOOP);
                case R_END   -> state.setAndContinue(TURN_R_END);
                case L_START -> state.setAndContinue(TURN_L_START);
                case L_TURN  -> state.setAndContinue(TURN_L_LOOP);
                case L_END   -> state.setAndContinue(TURN_L_END);
                default      -> state.setAndContinue(IDLE_ANIM);
            };
        }));

        registrar.add(new AnimationController<>(this, "events_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack_right", ATTACK_ANIM)
                .triggerableAnim("attack_left",  ATTACK_LEFT_ANIM)
                .triggerableAnim("bite",         BITE_ANIM)
                .triggerableAnim("split",        SPLIT_ANIM)
                .triggerableAnim("multisplit",   MULTISPLIT_ANIM)
                .triggerableAnim("breath",       BREATH_ANIM)
                .triggerableAnim("summon_claws", SUMMON_ANIM)
                .triggerableAnim("split_volley", SVOLLEY_ANIM)
                .triggerableAnim("death",        DEATH_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
