package net.raptorzizi.fangs_n_claws.entity.carnivorous_plant;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class CarnivorousPlantSproutEntity extends PathfinderMob implements GeoEntity {

    // Constants

    public static final int STATE_OPEN   = 0;
    public static final int STATE_ATTACK = 1;
    public static final int STATE_CLOSED = 2;

    private static final double DETECT_HEIGHT   = 0.6;
    private static final int    DAMAGE_INTERVAL = 20;
    private static final int    DIGEST_TICKS    = 200;

    private static final Vec3 STUCK_SPEED = new Vec3(0.8F, 0.75, 0.8F);

    private static final int MEALS_MIN = 2;
    private static final int MEALS_MAX = 3;

    private static final ResourceKey<DamageType> PLANT_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, FangsClawsMod.id("carnivorous_plant"));

    // Synched data

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(CarnivorousPlantSproutEntity.class, EntityDataSerializers.INT);

    // Animations

    private static final RawAnimation OPEN_ANIM   = RawAnimation.begin().thenLoop("carnivorous_plant_open");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("carnivorous_plant_attack");
    private static final RawAnimation CLOSE_ANIM  = RawAnimation.begin().thenLoop("carnivorous_plant_close");

    // Fields

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int damageTimer = 0;
    private int digestTimer = 0;
    private int mealsEaten  = 0;
    private int mealsNeeded;

    private float  lockedYaw = 0.0F;
    private Double anchorX = null, anchorZ = null;

    // Setup

    public CarnivorousPlantSproutEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.mealsNeeded = MEALS_MIN + this.random.nextInt(MEALS_MAX - MEALS_MIN + 1);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,           8.0)
                .add(Attributes.MOVEMENT_SPEED,       0.0)
                .add(Attributes.ATTACK_DAMAGE,        3.0)
                .add(Attributes.FOLLOW_RANGE,         0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, STATE_OPEN);
    }

    // State

    public int getState()            { return this.entityData.get(STATE); }
    private void setState(int state) { this.entityData.set(STATE, state); }

    public void lockYaw(float yaw) {
        this.lockedYaw = Math.round(yaw / 90.0F) * 90.0F;
        this.setYRot(this.lockedYaw);
        this.yRotO     = this.lockedYaw;
        this.yBodyRot  = this.lockedYaw;
        this.yBodyRotO = this.lockedYaw;
        this.yHeadRot  = this.lockedYaw;
        this.yHeadRotO = this.lockedYaw;
        this.setXRot(0.0F);
        this.xRotO = 0.0F;
    }

    // Immobility

    @Override public void knockback(double strength, double x, double z)   { }
    @Override public void push(double x, double y, double z)               { }
    @Override public boolean isPushable()                                  { return false; }
    @Override public boolean canBeCollidedWith()                           { return false; }
    @Override protected void pushEntities()                                { }
    @Override public boolean isPushedByFluid()                             { return false; }
    @Override public boolean ignoreExplosion(@NotNull Explosion explosion) { return true; }

    // Despawn

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // Tick

    @Override
    public void tick() {
        super.tick();

        this.lockYaw(this.lockedYaw);
        if (this.anchorX == null) {
            this.anchorX = this.getX();
            this.anchorZ = this.getZ();
        } else if (this.getX() != this.anchorX || this.getZ() != this.anchorZ) {
            this.setPos(this.anchorX, this.getY(), this.anchorZ);
        }

        if (this.level().isClientSide) return;

        if (this.getState() == STATE_CLOSED) {
            this.tickDigestion();
        } else {
            this.tickTrap();
        }
    }

    private void tickTrap() {
        List<LivingEntity> prey = this.preyOnTop();

        if (prey.isEmpty()) {
            if (this.getState() != STATE_OPEN) {
                this.setState(STATE_OPEN);
                this.playSound(SoundsRegistry.CARNIVOROUS_PLANT_OPEN.get(), 1.0F, this.pitch());
            }
            this.damageTimer = 0;
            return;
        }

        if (this.getState() != STATE_ATTACK) {
            this.setState(STATE_ATTACK);
            this.damageTimer = 0;
        }

        for (LivingEntity victim : prey) {
            victim.makeStuckInBlock(Blocks.AIR.defaultBlockState(), STUCK_SPEED);
        }

        if (++this.damageTimer < DAMAGE_INTERVAL) return;
        this.damageTimer = 0;

        DamageSource source = new DamageSource(this.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PLANT_DAMAGE), this);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);

        this.playSound(SoundsRegistry.CARNIVOROUS_PLANT_BITE.get(), 1.0F, this.pitch());

        boolean killed = false;
        for (LivingEntity victim : prey) {
            victim.hurt(source, damage);
            if (!victim.isAlive()) killed = true;
        }

        if (killed) this.startDigesting();
    }

    private void startDigesting() {
        this.mealsEaten++;
        this.setState(STATE_CLOSED);
        this.digestTimer = DIGEST_TICKS;
        this.damageTimer = 0;
        this.playSound(SoundsRegistry.CARNIVOROUS_PLANT_CLOSE.get(), 1.0F, this.pitch());
    }

    private void tickDigestion() {
        if (--this.digestTimer > 0) return;

        if (this.mealsEaten >= this.mealsNeeded) {
            this.growUp();
            return;
        }

        this.setState(STATE_OPEN);
        this.playSound(SoundsRegistry.CARNIVOROUS_PLANT_OPEN.get(), 1.0F, this.pitch());
    }

    private List<LivingEntity> preyOnTop() {
        AABB area = this.getBoundingBox().inflate(0.1, 0.0, 0.1)
                .expandTowards(0.0, DETECT_HEIGHT, 0.0);

        return this.level().getEntitiesOfClass(LivingEntity.class, area, e ->
                e != this
                        && e.isAlive()
                        && !(e instanceof CarnivorousPlantSproutEntity)
                        && !(e instanceof CarnivorousPlantEntity)
                        && !(e instanceof Player player && (player.isCreative() || player.isSpectator())));
    }

    private float pitch() {
        return 0.9F + this.random.nextFloat() * 0.2F;
    }

    // Grow up

    private void growUp() {
        if (!(this.level() instanceof ServerLevel server)) return;

        CarnivorousPlantEntity adult = EntityRegistry.CARNIVOROUS_PLANT.get().create(server);
        if (adult == null) return;

        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.lockedYaw, 0.0F);
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(adult.blockPosition()),
                MobSpawnType.CONVERSION, null);
        if (this.hasCustomName()) {
            adult.setCustomName(this.getCustomName());
            adult.setCustomNameVisible(this.isCustomNameVisible());
        }
        server.addFreshEntity(adult);
        this.discard();
    }

    // Save data

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State",       this.getState());
        tag.putInt("DigestTimer", this.digestTimer);
        tag.putInt("MealsEaten",  this.mealsEaten);
        tag.putInt("MealsNeeded", this.mealsNeeded);
        tag.putFloat("LockedYaw", this.lockedYaw);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setState(tag.getInt("State"));
        this.digestTimer = tag.getInt("DigestTimer");
        this.mealsEaten  = tag.getInt("MealsEaten");
        if (tag.contains("MealsNeeded")) this.mealsNeeded = tag.getInt("MealsNeeded");
        this.lockYaw(tag.getFloat("LockedYaw"));
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "state_controller", 0, state -> {
            switch (this.getState()) {
                case STATE_ATTACK -> state.setAndContinue(ATTACK_ANIM);
                case STATE_CLOSED -> state.setAndContinue(CLOSE_ANIM);
                default           -> state.setAndContinue(OPEN_ANIM);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
