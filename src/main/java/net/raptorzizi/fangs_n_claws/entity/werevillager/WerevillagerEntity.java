package net.raptorzizi.fangs_n_claws.entity.werevillager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.raptorzizi.fangs_n_claws.util.BiomeUtils;

import javax.annotation.Nullable;

public class WerevillagerEntity extends AbstractVillager {

    private static final EntityDataAccessor<String> BIOME_FOLDER =
            SynchedEntityData.defineId(WerevillagerEntity.class, EntityDataSerializers.STRING);

    private boolean transforming = false;
    private int     tcounter     = 0;

    // Spawn

    public WerevillagerEntity(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BIOME_FOLDER, "plains");
    }

    public String getBiomeFolder() {
        return this.entityData.get(BIOME_FOLDER);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        this.entityData.set(BIOME_FOLDER, BiomeUtils.resolveBiomeFolder(level, this.blockPosition()));
        return data;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,     20.0)
                .add(Attributes.MOVEMENT_SPEED,  0.25)
                .add(Attributes.FOLLOW_RANGE,   48.0);
    }

    // Sounds

    @Override protected SoundEvent getAmbientSound()              { return SoundEvents.VILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.VILLAGER_HURT;   }
    @Override protected SoundEvent getDeathSound()                { return SoundEvents.VILLAGER_DEATH;  }

    // Behavior

    @Override
    public boolean isTrading() { return false; }

    @Override
    public void openTradingScreen(Player player, Component displayName, int level) { }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    protected void updateTrades() { }

    @Override
    public void rewardTradeXp(MerchantOffer offer) { }

    @Override
    @Nullable
    public WerevillagerEntity getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    // AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // Transformation

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (!this.level().isDay() && !transforming && this.random.nextInt(125) == 0) {
            transforming = true;
            tcounter = 0;
        }

        if (transforming) {
            tcounter++;

            double shakeOffset = (tcounter % 2 == 0) ? 0.6 : -0.6;
            this.setPos(this.getX() + shakeOffset, this.getY(), this.getZ());

            if (tcounter % 2 == 0) {
                this.hurt(this.level().damageSources().magic(), 1.0F);
            }

            if (tcounter == 15) {
                this.playSound(SoundsRegistry.WEREWOLF_HOWL.get(), 2.0F,
                        0.8F + this.random.nextFloat() * 0.3F);
            }

            if (tcounter > 30) {
                spawnWerewolf();
            }
        }
    }

    private void spawnWerewolf() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.isDeadOrDying()) return;

        for (int i = 0; i < 30; i++) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    this.getX() + (this.random.nextDouble() - 0.5),
                    this.getY() + this.getBbHeight() * 0.5 + (this.random.nextDouble() - 0.5),
                    this.getZ() + (this.random.nextDouble() - 0.5),
                    0,
                    (this.random.nextDouble() - 0.5) * 0.4,
                    this.random.nextDouble() * 0.4,
                    (this.random.nextDouble() - 0.5) * 0.4,
                    0.15);
        }

        var werewolf = EntityRegistry.WEREWOLF.get().create(serverLevel);
        if (werewolf != null) {
            werewolf.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            werewolf.finalizeSpawn(serverLevel,
                    serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            serverLevel.addFreshEntity(werewolf);
        }

        this.discard();
    }

    // NBT

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("BiomeFolder", this.entityData.get(BIOME_FOLDER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BiomeFolder")) {
            this.entityData.set(BIOME_FOLDER, tag.getString("BiomeFolder"));
        }
    }
}
