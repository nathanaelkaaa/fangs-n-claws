package net.raptorzizi.fangs_n_claws.entity.mimic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MimicEntity extends Monster implements GeoEntity {

    private static final int   CATCH_TICKS = 20;
    private static final int   CHEW_TICKS  = 35;
    private static final float CHEW_DAMAGE = 2.0f;

    private static final EntityDataAccessor<Boolean> REVEALED =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM      = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_OPEN_ANIM = RawAnimation.begin().thenLoop("idle_open");
    private static final RawAnimation CATCH_ANIM     = RawAnimation.begin().then("catch",       Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation CHEW_ANIM      = RawAnimation.begin().then("attack_chew", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation JUMP_ANIM      = RawAnimation.begin().then("jump",        Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Player trappedPlayer  = null;
    private int    catchTicks     = 0;
    private int    chewTicks      = 0;
    private Vec3   catchStartPos  = Vec3.ZERO;
    private Vec3   mouthPos       = Vec3.ZERO;

    private int jumpCooldown   = 0;
    private int lastContactHit = 0;

    private final NonNullList<ItemStack> storedLoot = NonNullList.create();

    public MimicEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           30.0)
                .add(Attributes.ATTACK_DAMAGE,         4.0)
                .add(Attributes.MOVEMENT_SPEED,        0.25)
                .add(Attributes.FOLLOW_RANGE,         24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE,  0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(REVEALED, false);
    }

    public boolean isRevealed()             { return this.entityData.get(REVEALED); }
    public void    setRevealed(boolean rev) { this.entityData.set(REVEALED, rev); }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false) {
            @Override public boolean canUse() { return MimicEntity.this.isRevealed() && super.canUse(); }
        });
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!isRevealed() && trappedPlayer == null && this.isAlive()) {
            if (!this.level().isClientSide) {
                if (player.getAbilities().instabuild || player.isSpectator()) {
                    setRevealed(true);
                } else {
                    beginCatch(player);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void beginCatch(Player player) {
        setRevealed(true);
        this.trappedPlayer = player;
        this.catchTicks    = CATCH_TICKS;
        this.catchStartPos = player.position();

        Vec3 toPlayer = player.position().subtract(this.position());
        Vec3 dir = new Vec3(toPlayer.x, 0, toPlayer.z);
        dir = dir.lengthSqr() > 1.0e-4 ? dir.normalize() : Vec3.ZERO;
        this.mouthPos = this.position().add(dir.scale(0.4));

        float yaw = (float) (Mth.atan2(toPlayer.z, toPlayer.x) * Mth.RAD_TO_DEG) - 90f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;

        triggerAnim("main", "catch");
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && !isRevealed()) {
            setRevealed(true);
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (trappedPlayer != null) {
            tickTrap();
        } else if (isRevealed() && this.isAlive()) {
            tickSlimeJump();
        } else {
            float snapped = Math.round(this.getYRot() / 90f) * 90f;
            this.setYRot(snapped);
            this.setYBodyRot(snapped);
            this.setYHeadRot(snapped);
        }
    }

    private void tickTrap() {
        Player target = trappedPlayer;
        if (target == null) return;
        if (!target.isAlive() || target.isRemoved() || target.level() != this.level()
                || target.isSpectator() || target.getAbilities().instabuild || this.isDeadOrDying()) {
            releaseTrapped();
            return;
        }

        if (catchTicks > 0) {
            catchTicks--;
            float progress = 1f - (float) catchTicks / CATCH_TICKS;
            holdAt(target, catchStartPos.lerp(mouthPos, progress));
            if (catchTicks == 0) {
                chewTicks = CHEW_TICKS;
                triggerAnim("main", "chew");
            }
        } else if (chewTicks > 0) {
            chewTicks--;
            holdAt(target, mouthPos);
            if (chewTicks % 10 == 5) {
                if (target.hurt(this.damageSources().mobAttack(this), CHEW_DAMAGE)) {
                    target.invulnerableTime = 0;
                }
            }
            if (chewTicks == 0) {
                releaseTrapped();
            }
        } else {
            releaseTrapped();
        }
    }

    private void holdAt(Player target, Vec3 pos) {
        target.teleportTo(pos.x, pos.y, pos.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.resetFallDistance();
    }

    private void releaseTrapped() {
        if (trappedPlayer != null && trappedPlayer.isAlive()
                && !trappedPlayer.isSpectator() && !trappedPlayer.getAbilities().instabuild) {
            this.setTarget(trappedPlayer);
        }
        trappedPlayer = null;
        catchTicks = 0;
        chewTicks = 0;
    }

    private void tickSlimeJump() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        this.getLookControl().setLookAt(target);

        if (jumpCooldown > 0) {
            jumpCooldown--;
            return;
        }
        if (!this.onGround() || this.distanceToSqr(target) > 16 * 16) return;

        Vec3 toTarget = target.position().subtract(this.position());
        Vec3 dir = new Vec3(toTarget.x, 0, toTarget.z);
        if (dir.lengthSqr() > 1.0e-4) dir = dir.normalize();

        float yaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * Mth.RAD_TO_DEG) - 90f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;

        this.setDeltaMovement(dir.x * 0.7, 0.55, dir.z * 0.7);
        this.hasImpulse = true;
        triggerAnim("main", "jump");
        jumpCooldown = 25 + this.random.nextInt(15);
    }

    @Override
    public void playerTouch(@NotNull Player player) {
        if (!this.level().isClientSide && isRevealed() && trappedPlayer == null
                && this.isAlive() && this.tickCount - lastContactHit >= 20
                && this.getTarget() == player) {
            if (this.doHurtTarget(player)) {
                lastContactHit = this.tickCount;
            }
        }
    }

    @Override
    public boolean isPushable() {
        return isRevealed() && super.isPushable();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void consumeChest(BlockPos chestPos) {
        Level level = this.level();
        BlockState chestState = level.getBlockState(chestPos);

        if (level.getBlockEntity(chestPos) instanceof BaseContainerBlockEntity chest) {
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (!stack.isEmpty()) {
                    storedLoot.add(stack.copy());
                }
                chest.setItem(i, ItemStack.EMPTY);
            }
        }

        Direction facing = chestState.hasProperty(ChestBlock.FACING)
                ? chestState.getValue(ChestBlock.FACING) : Direction.NORTH;
        if (facing.getAxis() == Direction.Axis.Y) facing = Direction.NORTH;
        float yaw = facing.toYRot();
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        level.removeBlock(chestPos, false);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        for (ItemStack stack : storedLoot) {
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
            }
        }
        storedLoot.clear();
    }

    // NBT

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Revealed", isRevealed());

        if (!storedLoot.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : storedLoot) {
                if (!stack.isEmpty()) {
                    list.add(stack.save(this.registryAccess()));
                }
            }
            tag.put("StoredLoot", list);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setRevealed(tag.getBoolean("Revealed"));

        storedLoot.clear();
        ListTag list = tag.getList("StoredLoot", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(this.registryAccess(), list.getCompound(i)).ifPresent(storedLoot::add);
        }
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3,
                state -> state.setAndContinue(isRevealed() ? IDLE_OPEN_ANIM : IDLE_ANIM))
                .triggerableAnim("catch", CATCH_ANIM)
                .triggerableAnim("chew",  CHEW_ANIM)
                .triggerableAnim("jump",  JUMP_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
