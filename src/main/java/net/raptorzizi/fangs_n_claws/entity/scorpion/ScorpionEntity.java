package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ScorpionEntity extends Spider implements GeoEntity {

    private static final int ATTACK_HIT_TICK      = 9;
    private static final int ATTACK_TOTAL_TICKS   = 15;
    private static final int STINGER_HIT_TICK     = 10;
    private static final int STINGER_TOTAL_TICKS  = 15;
    private static final int STINGER_COOLDOWN_MAX = 200;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity  pendingAttackTarget = null;
    private int     attackDelayTick     = 0;
    private int     stingerCooldown     = 0;
    private boolean isStingerAttack     = false;

    private static final RawAnimation IDLE_ANIM           = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM           = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK1_ANIM        = RawAnimation.begin().then("attack1",        Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK2_ANIM        = RawAnimation.begin().then("attack2",        Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_STINGER_ANIM = RawAnimation.begin().then("attack_stinger", Animation.LoopType.PLAY_ONCE);

    public ScorpionEntity(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Spider>) entityType, level);
    }

    public static boolean checkScorpionSpawnRules(EntityType<? extends ScorpionEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH,    18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE,  4.0)
                .add(Attributes.FOLLOW_RANGE,  24.0);
    }

    // Jockey

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (level instanceof ServerLevel serverLevel
                && serverLevel.random.nextInt(100) == 0) {
            Skeleton skeleton = EntityType.SKELETON.create(serverLevel);
            if (skeleton != null) {
                skeleton.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                skeleton.finalizeSpawn(serverLevel, difficulty, MobSpawnType.MOB_SUMMONED, null);
                serverLevel.addFreshEntity(skeleton);
                skeleton.startRiding(this);
            }
        }
        return data;
    }

    // Goal

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Sound, Death

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource src) { return SoundsRegistry.SCORPION_HURT.get(); }
    @Override protected @NotNull SoundEvent getDeathSound() { return SoundsRegistry.SCORPION_DEATH.get(); }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (attackDelayTick > 0) return true;

        if (stingerCooldown <= 0 && this.random.nextBoolean()) {
            this.triggerAnim("attack_controller", "attack_stinger");
            this.playSound(SoundsRegistry.SCORPION_STINGER.get(), 1.0F, 1.0F);
            this.isStingerAttack = true;
            this.stingerCooldown = STINGER_COOLDOWN_MAX;
        } else {
            boolean useClaw1 = this.random.nextBoolean();
            this.triggerAnim("attack_controller", useClaw1 ? "attack1" : "attack2");
            this.playSound(useClaw1 ? SoundsRegistry.SCORPION_CLAW1.get() : SoundsRegistry.SCORPION_CLAW2.get(), 1.0F, 1.0F);
            this.isStingerAttack = false;
        }

        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    // Tick
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (stingerCooldown > 0) stingerCooldown--;

            if (attackDelayTick > 0) {
                attackDelayTick++;

                int hitTick    = isStingerAttack ? STINGER_HIT_TICK   : ATTACK_HIT_TICK;
                int totalTicks = isStingerAttack ? STINGER_TOTAL_TICKS : ATTACK_TOTAL_TICKS;

                if (attackDelayTick == hitTick) {
                    if (pendingAttackTarget != null
                            && pendingAttackTarget.isAlive()
                            && this.distanceTo(pendingAttackTarget) <= (this.getBbWidth() * 2.0F + pendingAttackTarget.getBbWidth() + 1.0F)) {

                        super.doHurtTarget(pendingAttackTarget);

                        if (pendingAttackTarget instanceof LivingEntity living) {
                            applyHitEffect(living, isStingerAttack);
                        }
                    }
                    pendingAttackTarget = null;
                }

                if (attackDelayTick >= totalTicks) {
                    attackDelayTick = 0;
                }
            }
        }
    }

    protected void applyHitEffect(LivingEntity living, boolean isStingerAttack) {
        if (isStingerAttack) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, true, true));
        }
    }

    // Variant

    public ResourceLocation textureLocation() {
        return FangsClawsMod.id("textures/entity/scorpion.png");
    }

    @Nullable
    public ResourceLocation eyesTexture() {
        return FangsClawsMod.id("textures/entity/glowing_eyes/scorpion_eyes.png");
    }

    public EyeStyle eyeStyle() {
        return EyeStyle.EYES;
    }

    public enum EyeStyle { NONE, EYES, EMISSIVE }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(
                new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                        .triggerableAnim("attack1",        ATTACK1_ANIM)
                        .triggerableAnim("attack2",        ATTACK2_ANIM)
                        .triggerableAnim("attack_stinger", ATTACK_STINGER_ANIM)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
