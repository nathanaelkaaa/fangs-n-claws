package net.raptorzizi.fangs_n_claws.entity.undead_horse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMob;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMobStruggleGoal;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public abstract class UndeadHorseMob extends HorseMob {

    private static final RawAnimation IDLE_ANIM         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_SLOW_ANIM    = RawAnimation.begin().thenLoop("walk_slow");
    private static final RawAnimation WALK_ANIM         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM          = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation EATING_GRASS_ANIM = RawAnimation.begin().thenLoop("eating_grass");

    protected UndeadHorseMob(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
    }

    // Goal

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HorseMobStruggleGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Tick

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.level().isDay() && !this.isWearingArmor() && !this.fireImmune()) {
            float brightness = this.getLightLevelDependentMagicValue();
            BlockPos eyePos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean sheltered = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            if (brightness > 0.5F && !sheltered && this.level().canSeeSky(eyePos)) {
                this.igniteForSeconds(8);
            }
        }
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level().isClientSide) {
            this.triggerAnim("attack_controller", "rear");
        }
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        this.addAttackController(registrar);

        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isEating()) return state.setAndContinue(EATING_GRASS_ANIM);
            if (this.isJumping()) return state.setAndContinue(REAR_ANIM);

            float animSpeed = this.walkAnimation.speed();
            if (animSpeed > 0.55f) return state.setAndContinue(RUN_ANIM);
            if (animSpeed > 0.25f) return state.setAndContinue(WALK_ANIM);
            if (animSpeed > 0.05f) return state.setAndContinue(WALK_SLOW_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }
}
