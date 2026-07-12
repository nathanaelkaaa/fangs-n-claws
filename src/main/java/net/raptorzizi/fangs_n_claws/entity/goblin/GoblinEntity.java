package net.raptorzizi.fangs_n_claws.entity.goblin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.registries.GameRuleRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import java.util.ArrayList;
import java.util.List;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GoblinEntity extends Monster implements GeoEntity {

    // Variables

    private static final EntityDataAccessor<Boolean> IS_AGGRESSIVE =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double CHASE_SPEED          = 0.45;
    private static final double FLEE_SPEED           = 0.58;
    private static final double ORBIT_SPEED          = 0.65;
    private static final double RUSH_SPEED           = 0.55;
    private static final double ORBIT_RADIUS         = 4.5;
    private static final double ORBIT_ANGULAR_SPEED  = 0.025;
    private static final double ALLY_SCAN_RANGE      = 12.0;
    private static final double LOOK_AWAY_THRESHOLD  = 0.3;
    private static final int    ORBIT_DIR_CHANGE_MIN = 80;
    private static final int    ORBIT_DIR_CHANGE_MAX = 200;
    private static final int    FLEE_TICKS           = 30;
    private static final int    ATTACK_COOLDOWN_MAX  = 80;
    private static final int    ATTACK_HIT_TICK      = 8;
    private static final int    ATTACK_TOTAL_TICKS   = 18;
    private static final double STEAL_FLEE_SPEED     = 0.78;
    private static final int    STEAL_HIT_TICK       = 12;
    private static final int    STEAL_TOTAL_TICKS    = 24;
    private static final int    STEAL_FLEE_TICKS     = 65;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity  pendingTarget       = null;
    private int     attackTick          = 0;
    private int     stealTick           = 0;
    private boolean stealFleeMode       = false;
    private int     fleeTick            = 0;
    private int     attackCooldown      = 0;
    private float   orbitAngle;
    private int     orbitDirection      = 1;
    private int     orbitDirChangeTick  = 0;
    private double  prevCombatX         = 0;
    private double  prevCombatZ         = 0;
    private int     combatStuckTick     = 0;

    private static final int    COMBAT_STUCK_THRESHOLD = 5;
    private static final double COMBAT_STUCK_MIN_MOVE  = 0.0025; // 0.05 blocks/tick squared

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM    = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation STEAL_ANIM  = RawAnimation.begin().then("steal",  Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation RIDING_ANIM = RawAnimation.begin().thenLoop("riding");

    // Spawn

    public GoblinEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
        this.orbitAngle       = (float)(this.random.nextDouble() * Math.PI * 2.0);
        this.orbitDirection   = this.random.nextBoolean() ? 1 : -1;
        this.orbitDirChangeTick = ORBIT_DIR_CHANGE_MIN + this.random.nextInt(ORBIT_DIR_CHANGE_MAX - ORBIT_DIR_CHANGE_MIN);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_AGGRESSIVE, false);
    }

    public boolean isAggressive()               { return this.entityData.get(IS_AGGRESSIVE); }
    public void    setAggressive(boolean value) { this.entityData.set(IS_AGGRESSIVE, value); }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    protected float getEquipmentDropChance(EquipmentSlot slot) {
        return 0f;
    }

    @Override
    public boolean canControlVehicle() {
        return false;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               10.0)
                .add(Attributes.MOVEMENT_SPEED,            0.28)
                .add(Attributes.ATTACK_DAMAGE,             3.0)
                .add(Attributes.FOLLOW_RANGE,             20.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE,  1.6);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Sound, Death

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.GOBLIN_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.GOBLIN_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.GOBLIN_DEATH.get(); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
            if (!offhand.isEmpty()) {
                this.spawnAtLocation(offhand);
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
        return hurt;
    }

    // Combat

    @Override
    public boolean isCurrentlyGlowing() {
        return !this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() || super.isCurrentlyGlowing();
    }

    public boolean isAttacking() { return attackTick > 0; }
    public boolean isStealing()  { return stealTick  > 0; }
    public boolean isActing()    { return attackTick > 0 || stealTick > 0; }

    private void triggerAttack(LivingEntity target) {
        if (isActing() || attackCooldown > 0) return;
        if (target instanceof Player && this.random.nextInt(4) == 0
                && this.level().getGameRules().getBoolean(GameRuleRegistry.ALLOW_GOBLIN_STEALING)
                && CommonConfigs.ALLOW_GOBLIN_STEALING.get()) {
            this.triggerAnim("attack_controller", "steal");
            this.pendingTarget = target;
            this.stealTick     = 1;
        } else {
            this.triggerAnim("attack_controller", "attack");
            this.pendingTarget = target;
            this.attackTick    = 1;
        }
    }

    private void performSteal(LivingEntity target) {
        if (!(target instanceof Player player)) return;
        List<Integer> nonEmpty = new ArrayList<>();
        for (int i = 9; i <= 35; i++) {
            if (!player.getInventory().getItem(i).isEmpty()) nonEmpty.add(i);
        }
        if (nonEmpty.isEmpty()) return;

        int slot = nonEmpty.get(this.random.nextInt(nonEmpty.size()));
        ItemStack stolen = player.getInventory().getItem(slot).copy();
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, stolen);
        this.playSound(SoundsRegistry.GOBLIN_CELEBRATE.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        if (player instanceof ServerPlayer sp) {
            FncAdvancements.grant(sp, "forge/smooth_criminal");
        }

        stealFleeMode  = true;
        fleeTick       = STEAL_FLEE_TICKS;
        attackCooldown = ATTACK_COOLDOWN_MAX * 2;
    }

    private int countNearbyAllies() {
        return this.level().getEntitiesOfClass(
                GoblinEntity.class,
                this.getBoundingBox().inflate(ALLY_SCAN_RANGE),
                g -> g != this && g.isAlive() && g.getTarget() != null
        ).size();
    }

    private boolean isTargetLookingAway(LivingEntity target) {
        Vec3 targetLook = target.getLookAngle();
        Vec3 toSelf     = this.position().subtract(target.position()).normalize();
        return targetLook.dot(toSelf) < LOOK_AWAY_THRESHOLD;
    }

    // Tick

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (attackCooldown > 0) attackCooldown--;

            if (attackTick > 0) {
                attackTick++;
                if (attackTick == ATTACK_HIT_TICK) {
                    if (pendingTarget != null && pendingTarget.isAlive()
                            && this.distanceTo(pendingTarget) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 0.5) {
                        super.doHurtTarget(pendingTarget);
                    }
                    pendingTarget = null;
                }
                if (attackTick >= ATTACK_TOTAL_TICKS) attackTick = 0;
            }

            if (stealTick > 0) {
                stealTick++;
                if (stealTick == STEAL_HIT_TICK) {
                    if (pendingTarget instanceof LivingEntity lt && lt.isAlive()
                            && this.distanceTo(lt) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 0.8) {
                        performSteal(lt);
                    }
                    pendingTarget = null;
                }
                if (stealTick >= STEAL_TOTAL_TICKS) stealTick = 0;
            }

            boolean aggressive = this.getTarget() != null && this.getTarget().isAlive();
            if (aggressive != isAggressive()) setAggressive(aggressive);

            tickCombat();
        }
    }

    private void tickCombat() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            combatStuckTick = 0;
            return;
        }

        if (this.isPassenger()) {
            tickMountedBehavior(target);
            return;
        }

        double movedSq = Math.pow(this.getX() - prevCombatX, 2) + Math.pow(this.getZ() - prevCombatZ, 2);
        if (!isActing() && movedSq < COMBAT_STUCK_MIN_MOVE) {
            combatStuckTick++;
            if (combatStuckTick >= COMBAT_STUCK_THRESHOLD && this.onGround()) {
                this.getJumpControl().jump();
                combatStuckTick = 0;
            }
        } else {
            combatStuckTick = 0;
        }
        prevCombatX = this.getX();
        prevCombatZ = this.getZ();

        if (countNearbyAllies() >= 1) {
            tickGroupBehavior(target);
        } else {
            tickSoloBehavior(target);
        }
    }

    private void tickMountedBehavior(LivingEntity target) {
        if (this.getVehicle() instanceof WildWolfEntity wolf && wolf.getTarget() != target) {
            wolf.setTarget(target);
        }

        if (isActing()) return;

        faceToward(new Vec3(target.getX(), target.getY(), target.getZ()));

        if (this.distanceTo(target) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                && attackCooldown == 0) {
            triggerAttack(target);
            attackCooldown = ATTACK_COOLDOWN_MAX;
        }
    }
    private void tickSoloBehavior(LivingEntity target) {
        if (isActing()) return;

        if (fleeTick > 0) {
            fleeTick--;
            double fleeSpeed = stealFleeMode ? STEAL_FLEE_SPEED : FLEE_SPEED;
            Vec3 fleeDir    = this.position().subtract(target.position());
            Vec3 fleeTarget = this.position().add(
                    fleeDir.length() > 0.01 ? fleeDir.normalize().scale(1.5) : new Vec3(1, 0, 0));
            faceToward(fleeTarget);
            steerToward(fleeTarget, fleeSpeed, 0.22);
            if (fleeTick == 0) stealFleeMode = false;
            return;
        }

        Vec3 chaseTarget = new Vec3(target.getX(), target.getY(), target.getZ());
        faceToward(chaseTarget);

        if (this.distanceTo(target) <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                && attackCooldown == 0) {
            triggerAttack(target);
            fleeTick       = FLEE_TICKS;
            attackCooldown = ATTACK_COOLDOWN_MAX;
        } else if (this.hurtTime == 0) {
            steerToward(chaseTarget, CHASE_SPEED, 0.20);
        }
    }

    private void tickGroupBehavior(LivingEntity target) {
        if (isActing()) return;

        if (fleeTick > 0 && stealFleeMode) {
            fleeTick--;
            Vec3 fleeDir    = this.position().subtract(target.position());
            Vec3 fleeTarget = this.position().add(
                    fleeDir.length() > 0.01 ? fleeDir.normalize().scale(1.5) : new Vec3(1, 0, 0));
            faceToward(fleeTarget);
            steerToward(fleeTarget, STEAL_FLEE_SPEED, 0.25);
            if (fleeTick == 0) stealFleeMode = false;
            return;
        }

        boolean lookingAway = isTargetLookingAway(target);
        double  dist        = this.distanceTo(target);

        if (lookingAway && attackCooldown == 0) {
            Vec3 rushTarget = new Vec3(target.getX(), target.getY(), target.getZ());
            faceToward(rushTarget);

            if (dist <= this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) {
                triggerAttack(target);
                attackCooldown = ATTACK_COOLDOWN_MAX;
            } else {
                steerToward(rushTarget, RUSH_SPEED, 0.25);
            }
        } else {
            if (--orbitDirChangeTick <= 0) {
                orbitDirection      = -orbitDirection;
                orbitDirChangeTick  = ORBIT_DIR_CHANGE_MIN + this.random.nextInt(ORBIT_DIR_CHANGE_MAX - ORBIT_DIR_CHANGE_MIN);
            }
            orbitAngle += (float)(ORBIT_ANGULAR_SPEED * orbitDirection);

            double ox = target.getX() + Math.cos(orbitAngle) * ORBIT_RADIUS;
            double oz = target.getZ() + Math.sin(orbitAngle) * ORBIT_RADIUS;
            Vec3 orbitTarget = new Vec3(ox, target.getY(), oz);

            faceToward(new Vec3(target.getX(), target.getY(), target.getZ()));
            steerToward(orbitTarget, ORBIT_SPEED, 0.15);
        }
    }

    private void steerToward(Vec3 target, double speed, double lerpFactor) {
        if (this.isInWater()) speed *= 0.4;
        double dx    = target.x - this.getX();
        double dz    = target.z - this.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist > 0.2) {
            Vec3 current = this.getDeltaMovement();
            double wx = (dx / hDist) * speed;
            double wz = (dz / hDist) * speed;
            this.setDeltaMovement(
                    current.x + (wx - current.x) * lerpFactor,
                    current.y,
                    current.z + (wz - current.z) * lerpFactor
            );
        }
    }

    private void faceToward(Vec3 target) {
        double dx = target.x - this.getX();
        double dz = target.z - this.getZ();
        if (dx * dx + dz * dz < 0.01) return;
        float yaw = (float)(Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isPassenger()) return state.setAndContinue(RIDING_ANIM);
            if (isAttacking() || isStealing()) return PlayState.STOP;
            double speed = this.getDeltaMovement().horizontalDistance();
            if (speed > 0.18) return state.setAndContinue(RUN_ANIM);
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM)
                .triggerableAnim("steal", STEAL_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
