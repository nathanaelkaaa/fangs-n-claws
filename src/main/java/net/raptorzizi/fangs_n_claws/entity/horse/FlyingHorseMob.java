package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class FlyingHorseMob extends HorseMob {

    private static final EntityDataAccessor<Boolean> IS_FLYING =
            SynchedEntityData.defineId(FlyingHorseMob.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FLAPPING =
            SynchedEntityData.defineId(FlyingHorseMob.class, EntityDataSerializers.BOOLEAN);

    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private final MoveControl    groundMoveControl;
    private final MoveControl    flyingMoveControl;

    private int    modeCooldown      = 0;
    private int    flapAnimTick      = 0;
    private int    flapPushCountdown = 0;
    private float  flapCharge        = 0f;
    private double airSpeed          = 0.0;

    protected FlyingHorseMob(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
        this.groundNavigation  = this.navigation;
        this.groundMoveControl = this.moveControl;
        FlyingPathNavigation flyNav = new FlyingPathNavigation(this, level);
        flyNav.setCanOpenDoors(false);
        flyNav.setCanFloat(true);
        flyNav.setCanPassDoors(false);
        this.flyingNavigation  = flyNav;
        this.flyingMoveControl = new HorseFlyMoveControl(this);
    }

    protected int    flightHeadroom()     { return 3; }
    protected int    modeSwitchCooldown()  { return 20; }
    protected double glideFallSpeed()      { return 0.15; }
    protected int    flapAnimTicks()       { return 15; }
    protected int    flapPushDelay()       { return 10; }
    protected double flapJumpFactor()      { return 1.6; }
    protected double airMaxSpeed()         { return 0.5; }
    protected double airAccel()            { return 0.03; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_FLYING, false);
        this.entityData.define(IS_FLAPPING, false);
    }

    // Tick

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.setNoGravity(this.isFlying());

            if (flapAnimTick > 0) {
                if (flapAnimTick == flapAnimTicks() - flapPushDelay()) {
                    this.playSound(SoundEvents.PHANTOM_FLAP, 0.6F, 1.2F);
                }
                if (--flapAnimTick == 0) this.setFlapping(false);
            } else if (this.isFlying()) {
                Vec3 m = this.getDeltaMovement();
                boolean moving = m.horizontalDistanceSqr() > 0.0025 || Math.abs(m.y) > 0.04;
                if (moving != this.isFlapping()) this.setFlapping(moving);
            } else if (this.isFlapping()) {
                this.setFlapping(false);
            }
        } else if (flapPushCountdown > 0 && --flapPushCountdown == 0
                && !this.isInWater() && this.getControllingPassenger() instanceof Player) {
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, this.getJumpPower() * flapJumpFactor() * flapCharge, m.z);
            this.hasImpulse = true;
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (modeCooldown > 0) modeCooldown--;
        if (this.isInWater()) {
            if (this.isFlying()) {
                this.setFlying(false);
                modeCooldown = modeSwitchCooldown();
            }
        } else {
            LivingEntity target = this.getTarget();
            boolean wantFly = !(this.getFirstPassenger() instanceof Player)
                    && target != null && target.isAlive() && this.hasFlightHeadroom();
            if (wantFly != this.isFlying() && modeCooldown == 0) {
                this.setFlying(wantFly);
                modeCooldown = modeSwitchCooldown();
            }
        }
    }

    // Vol

    public boolean isFlying()   { return this.entityData.get(IS_FLYING); }
    public boolean isFlapping() { return this.entityData.get(IS_FLAPPING); }
    protected void setFlapping(boolean f) { this.entityData.set(IS_FLAPPING, f); }

    public void setFlying(boolean flying) {
        if (this.isFlying() == flying) return;
        this.entityData.set(IS_FLYING, flying);
        if (!this.level().isClientSide) {
            this.navigation.stop();
            this.navigation  = flying ? this.flyingNavigation  : this.groundNavigation;
            this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
            this.setNoGravity(flying);
        }
    }

    protected boolean hasFlightHeadroom() {
        BlockPos head = this.blockPosition().above(Mth.ceil(this.getBbHeight()));
        for (int i = 0; i < flightHeadroom(); i++) {
            if (this.level().getBlockState(head.above(i)).blocksMotion()) return false;
        }
        return true;
    }

    @Override
    public void onPlayerJump(int charge) {
        if (charge > 0 && this.isSaddled() && !this.isInWater() && this.getControllingPassenger() instanceof Player) {
            this.flapCharge = Math.min(1.0f, charge / 100.0f);
            this.flapPushCountdown = flapPushDelay();
        }
    }

    @Override
    public void handleStartJump(int charge) {
        if (charge <= 0) return;
        this.flapAnimTick = flapAnimTicks();
        this.setFlapping(true);
    }

    @Override
    public void handleStopJump() {
    }

    // Flying
    @Override
    public void travel(@NotNull Vec3 input) {
        LivingEntity controller = this.getControllingPassenger();
        boolean gliding = !this.onGround() && !this.isInWater() && controller instanceof Player;
        super.travel(input);
        if (gliding) {
            Vec3 m = this.getDeltaMovement();
            double y = m.y < -glideFallSpeed() ? -glideFallSpeed() : m.y;

            float fwd = controller.zza;
            float str = controller.xxa;
            if (fwd != 0f || str != 0f) {
                airSpeed = Math.min(airSpeed + airAccel(), airMaxSpeed());
                float yaw = this.getYRot() * Mth.DEG_TO_RAD;
                float sin = Mth.sin(yaw), cos = Mth.cos(yaw);
                double dirX = str * cos - fwd * sin;
                double dirZ = fwd * cos + str * sin;
                double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
                this.setDeltaMovement(dirX / len * airSpeed, y, dirZ / len * airSpeed);
            } else {
                airSpeed *= 0.9;
                this.setDeltaMovement(m.x * 0.9, y, m.z * 0.9);
            }
        } else {
            airSpeed = 0.0;
        }
    }
}
