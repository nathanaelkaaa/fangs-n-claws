package net.raptorzizi.fangs_n_claws.entity.hell_ogre;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreMoveControl;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HellOgreEntity extends OgreEntity {

    // Variables

    private static final int   FIRE_BREATH_TOTAL_TICKS  = 80;
    private static final int   FIRE_BREATH_START_TICK   = 10;
    private static final int   FIRE_BREATH_END_TICK     = 70;
    private static final int   FIRE_BREATH_SOUND_INTERVAL = 20;
    private static final float FIRE_BREATH_DAMAGE      = 3.0f;
    private static final float FIRE_BREATH_RANGE       = 10.0f;
    private static final float FIRE_BREATH_CONE_COS    = Mth.cos(40.0f * Mth.DEG_TO_RAD);

    private static final EntityDataAccessor<Boolean> IS_FIRE_BREATHING =
            SynchedEntityData.defineId(HellOgreEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int fireBreathTick = 0;

    private static final RawAnimation FIRE_BREATH_ANIM =
            RawAnimation.begin().then("fire_breath_attack", Animation.LoopType.PLAY_ONCE);

    // Spawn

    public HellOgreEntity(EntityType<? extends HellOgreEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new OgreMoveControl(this);
    }

    // 1.20.1: no Builder — define directly
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_FIRE_BREATHING, false);
    }

    // 1.20.1: 5-param finalizeSpawn
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        setSiamese(false);
        return result;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return OgreEntity.prepareAttributes()
                .add(Attributes.MAX_HEALTH,   70.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public static boolean checkHellOgreSpawnRules(EntityType<? extends HellOgreEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HellOgreAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected double getSlamDamage() {
        return 10.0;
    }

    // Fire breath

    public boolean isFireBreathActive() {
        return fireBreathTick > 0;
    }

    public boolean isFireBreathing() {
        return this.entityData.get(IS_FIRE_BREATHING);
    }

    private void setFireBreathing(boolean value) {
        this.entityData.set(IS_FIRE_BREATHING, value);
    }

    public void triggerFireBreath() {
        this.triggerAnim("fire_breath_controller", "fire_breath_attack");
        this.fireBreathTick = 1;
    }

    private Vec3 getHeadLookAngle() {
        return this.calculateViewVector(this.getXRot(), this.yHeadRot);
    }

    private void applyFireBreathDamage() {
        Vec3 look = getHeadLookAngle();
        double rangeSquared = FIRE_BREATH_RANGE * FIRE_BREATH_RANGE;

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(FIRE_BREATH_RANGE))) {
            if (target == this) continue;
            Vec3 toTarget = new Vec3(
                    target.getX() - this.getX(),
                    target.getY() + target.getBbHeight() * 0.5 - this.getEyeY(),
                    target.getZ() - this.getZ());
            if (toTarget.lengthSqr() > rangeSquared) continue;
            float dot = (float) look.dot(toTarget.normalize());
            if (dot < FIRE_BREATH_CONE_COS) continue;
            target.hurt(this.level().damageSources().mobAttack(this), FIRE_BREATH_DAMAGE);
            target.setRemainingFireTicks(80);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnFireBreathParticles() {
        Vec3 look = getHeadLookAngle().normalize();
        Vec3 eyeBase = new Vec3(this.getX(), this.getY() + this.getEyeHeight() * 0.7, this.getZ());
        Vec3 mouth = eyeBase.add(look.scale(1.5));

        RandomSource rng = this.random;
        double speed = rng.nextDouble() * 0.35 + 0.35;

        for (int i = 0; i < 8; i++) {
            double offset = 0.15;
            double ox = rng.nextDouble() * 2 * offset - offset;
            double oy = rng.nextDouble() * 2 * offset - offset;
            double oz = rng.nextDouble() * 2 * offset - offset;

            double angularness = 0.45;
            Vec3 randomVec = new Vec3(
                    rng.nextDouble() * 2 * angularness - angularness,
                    rng.nextDouble() * 2 * angularness - angularness,
                    rng.nextDouble() * 2 * angularness - angularness).normalize();
            Vec3 vel = look.scale(3.0).add(randomVec).normalize().scale(speed);

            this.level().addParticle(ParticlesRegistry.FIRE.get(),
                    mouth.x + ox, mouth.y + oy, mouth.z + oz,
                    vel.x, vel.y, vel.z);
        }
    }

    // Tick

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (fireBreathTick > 0) {
                if (fireBreathTick == 1) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsRegistry.OGRE_ROAR.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
                }

                fireBreathTick++;
                boolean activeWindow = fireBreathTick >= FIRE_BREATH_START_TICK
                                    && fireBreathTick <= FIRE_BREATH_END_TICK;
                setFireBreathing(activeWindow);
                if (activeWindow) {
                    applyFireBreathDamage();
                    if ((fireBreathTick - FIRE_BREATH_START_TICK) % FIRE_BREATH_SOUND_INTERVAL == 0) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundsRegistry.HELL_OGRE_FIRE_BREATH.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
                    }
                }
                if (fireBreathTick >= FIRE_BREATH_TOTAL_TICKS) {
                    fireBreathTick = 0;
                    setFireBreathing(false);
                }
            }
        } else {
            if (isFireBreathing()) {
                spawnFireBreathParticles();
            }
        }
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        super.registerControllers(registrar);
        registrar.add(new AnimationController<>(this, "fire_breath_controller", 0,
                state -> PlayState.STOP)
                .triggerableAnim("fire_breath_attack", FIRE_BREATH_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
