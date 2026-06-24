package net.raptorzizi.fangs_n_claws.entity.silver_skeleton;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class SilverSkeletonEntity extends Monster implements GeoEntity {

    // Variables

    private static final int    ATTACK_HIT_TICK    = 8;
    private static final int    ATTACK_TOTAL_TICKS = 18;
    private static final double ATTACK_RANGE       = 2.5;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private Entity pendingAttackTarget = null;
    private int    attackDelayTick     = 0;

    private static final RawAnimation IDLE_ANIM          = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM          = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RIDING_ANIM        = RawAnimation.begin().thenLoop("riding");
    private static final RawAnimation ATTACK_RIGHT_ANIM  = RawAnimation.begin().then("attack_right",  Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_LEFT_ANIM   = RawAnimation.begin().then("attack_left",   Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation ATTACK_DOUBLE_ANIM = RawAnimation.begin().then("attack_double", Animation.LoopType.PLAY_ONCE);

    // Spawn

    public SilverSkeletonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
                                        MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        SpawnGroupData data = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemsRegistry.SILVER_SWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND,  new ItemStack(ItemsRegistry.SILVER_SWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0F);
        this.setDropChance(EquipmentSlot.OFFHAND,  0F);
        return data;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH,               25.0)
                .add(Attributes.MOVEMENT_SPEED,            0.30)
                .add(Attributes.ATTACK_DAMAGE,             5.0)
                .add(Attributes.FOLLOW_RANGE,              24.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE,  2.5)
                .add(Attributes.KNOCKBACK_RESISTANCE,      0.1);
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, WerewolfEntity.class, false));
    }

    // Sound

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.SKELETON_DEATH; }

    // Combat

    public boolean isAttacking() { return attackDelayTick > 0; }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        String anim = switch (this.random.nextInt(3)) {
            case 0  -> "attack_right";
            case 1  -> "attack_left";
            default -> "attack_double";
        };
        this.triggerAnim("attack_controller", anim);
        this.pendingAttackTarget = target;
        this.attackDelayTick = 1;
        return true;
    }

    // Tick

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isSunBurnTick()) {
            this.igniteForSeconds(8);
        }

        super.tick();

        if (!this.level().isClientSide && attackDelayTick > 0) {
            attackDelayTick++;
            if (attackDelayTick == ATTACK_HIT_TICK) {
                if (pendingAttackTarget != null && pendingAttackTarget.isAlive()
                        && this.distanceTo(pendingAttackTarget) <= ATTACK_RANGE + 1.0
                        && this.hasLineOfSight(pendingAttackTarget)) {
                    super.doHurtTarget(pendingAttackTarget);
                }
                pendingAttackTarget = null;
            }
            if (attackDelayTick >= ATTACK_TOTAL_TICKS) attackDelayTick = 0;
        }
    }

    // Animation

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isPassenger()) return state.setAndContinue(RIDING_ANIM);
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack_right",  ATTACK_RIGHT_ANIM)
                .triggerableAnim("attack_left",   ATTACK_LEFT_ANIM)
                .triggerableAnim("attack_double", ATTACK_DOUBLE_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
