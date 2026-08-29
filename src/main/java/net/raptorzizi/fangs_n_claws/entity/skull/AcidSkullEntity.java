package net.raptorzizi.fangs_n_claws.entity.skull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

public class AcidSkullEntity extends SkullEntity {

    private static final int ACID_DURATION_TICKS = 200; // 10 s
    private static final int REGEN_CHANCE = 1000;

    private static final EntityDataAccessor<Boolean> HARVESTED =
            SynchedEntityData.defineId(AcidSkullEntity.class, EntityDataSerializers.BOOLEAN);

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/acid_skull_eyes.png");

    public AcidSkullEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HARVESTED, false);
    }

    public boolean isHarvested()            { return this.entityData.get(HARVESTED); }
    public void    setHarvested(boolean v)  { this.entityData.set(HARVESTED, v); }

    @Override
    public String textureBaseName() {
        return isHarvested() ? "acid_skull_harvested" : "acid_skull";
    }

    @Nullable
    @Override
    public ResourceLocation glowingEyesTexture() {
        return isHarvested() ? null : EYES;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && isHarvested() && this.random.nextInt(REGEN_CHANCE) == 0) {
            setHarvested(false);
        }
    }

    @Override
    protected void spawnAmbientParticles() {
        emitAround(ParticlesRegistry.ACID_CLOUD.get(), -0.01);
    }

    @Override
    protected void applyHitEffect(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffectsRegistry.ACID, ACID_DURATION_TICKS, 0, false, true, true));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Harvested", isHarvested());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHarvested(tag.getBoolean("Harvested"));
    }
}
