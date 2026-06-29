package net.raptorzizi.fangs_n_claws.entity.horse_bat;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.horse.FlyingHorseMob;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMobStruggleGoal;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class HorseBatEntity extends FlyingHorseMob {

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_SLOW_ANIM    = RawAnimation.begin().thenLoop("walk_slow");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM          = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation EATING_GRASS_ANIM = RawAnimation.begin().thenLoop("eating_grass");
    private static final RawAnimation FLY_HOVERING_ANIM = RawAnimation.begin().thenLoop("fly_hovering");
    private static final RawAnimation FLY_FLAP_ANIM     = RawAnimation.begin().thenLoop("fly_flap");

    public HorseBatEntity(EntityType<?> type, Level level) {
        super((EntityType<? extends AbstractHorse>) type, level);
    }

    // Goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HorseMobStruggleGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Spawn

    public static AttributeSupplier.Builder prepareAttributes() {
        return createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.225)
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected boolean shouldSpawnRider(RandomSource random) {
        return random.nextInt(3) != 0;
    }

    @Nullable
    @Override
    protected Mob createRider(ServerLevel level) {
        int roll = level.random.nextInt(20);
        return (roll < 11)
                ? EntityType.SKELETON.create(level)
                : (roll < 19)
                  ? EntityType.ZOMBIE.create(level)
                  : EntityRegistry.SILVER_SKELETON.get().create(level);
    }

    // Tick : auto-combustion au soleil (par-dessus la gestion du vol héritée)

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.level().isDay()) {
            float brightness = this.getLightLevelDependentMagicValue();
            BlockPos eyePos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean sheltered = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow
                    || this.isWearingArmor();
            if (brightness > 0.5F && !sheltered && this.level().canSeeSky(eyePos)) {
                this.igniteForSeconds(8);
            }
        }
    }

    // Sons

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        this.addAttackController(registrar);

        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (!this.isInWater() && (this.isFlying() || !this.onGround())) {
                if (this.isFlapping()) return state.setAndContinue(FLY_FLAP_ANIM);
                return state.setAndContinue(FLY_HOVERING_ANIM);
            }

            if (this.isEating()) return state.setAndContinue(EATING_GRASS_ANIM);

            float animSpeed = this.walkAnimation.speed();
            if (animSpeed > 0.55f) return state.setAndContinue(RUN_ANIM);
            if (animSpeed > 0.25f) return state.setAndContinue(WALK_ANIM);
            if (animSpeed > 0.05f) return state.setAndContinue(WALK_SLOW_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }
}
