package net.raptorzizi.fangs_n_claws.entity.hyena;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.BabyWildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.Nullable;

public class HyenaEntity extends WildWolfEntity {

    // Constants

    private static final double PLAYER_AGGRO_RANGE = 4.0;
    private static final int    EAT_TICKS          = 40;
    private static final int    EAT_EFFECT_PERIOD  = 4;
    private static final float  HEAL_AMOUNT        = 4.0F;

    // Fields

    private int eatTick = 0;

    // Setup

    public HyenaEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Taming

    @Override
    public TagKey<Item> foodTag() { return BabyHyenaEntity.HYENA_FOOD; }

    // Breeding

    @Override
    public EntityType<? extends BabyWildWolfEntity> babyType() { return EntityRegistry.BABY_HYENA.get(); }

    // AI

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override protected double getFollowDistance() { return PLAYER_AGGRO_RANGE; }
            @Override public boolean canUse()              { return !isTamed() && super.canUse(); }
            @Override public boolean canContinueToUse()    { return !isTamed() && super.canContinueToUse(); }
        });

        this.goalSelector.addGoal(4, new HyenaScavengeGoal(this));
    }

    // Sound

    @Override protected SoundEvent getAmbientSound()              { return SoundsRegistry.HYENA_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundsRegistry.HYENA_HURT.get(); }
    @Override protected SoundEvent getDeathSound()                { return SoundsRegistry.HYENA_DEATH.get(); }

    @Override protected SoundEvent getAttackSound()   { return SoundsRegistry.HYENA_AMBIENT.get(); }

    @Override protected float basePitch() { return 1.0F; }

    @Nullable
    @Override protected SoundEvent getPackHowlSound() { return null; }

    // Tick

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickEatHeldFood();
    }

    // Scavenging

    private void tickEatHeldFood() {
        ItemStack held = this.getMainHandItem();
        if (held.isEmpty() || this.getHealth() >= this.getMaxHealth()) {
            this.eatTick = 0;
            return;
        }

        if (++this.eatTick % EAT_EFFECT_PERIOD == 0) spawnEatEffects(held);

        if (this.eatTick >= EAT_TICKS) {
            this.heal(HEAL_AMOUNT);
            held.shrink(1);
            this.setItemSlot(EquipmentSlot.MAINHAND, held.isEmpty() ? ItemStack.EMPTY : held);
            this.eatTick = 0;
        }
    }

    private void spawnEatEffects(ItemStack held) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL, 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, held),
                    this.getX(), this.getEyeY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
