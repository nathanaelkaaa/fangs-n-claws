package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;

public class PoisonCloudEntity extends Entity {

    private static final int   DEFAULT_LIFETIME = 200;
    private static final int   POISON_INTERVAL  = 20;
    private static final int   POISON_DURATION  = 120;
    private static final int   PARTICLES_PER_TICK = 4;
    private static final int   ACID_BUBBLES_PER_TICK = 3;
    private static final int   PUDDLE_SOUND_INTERVAL = 74;

    private static final EntityDataAccessor<Float> DATA_WIDTH =
            SynchedEntityData.defineId(PoisonCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT =
            SynchedEntityData.defineId(PoisonCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_PARTICLES =
            SynchedEntityData.defineId(PoisonCloudEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ACID_POOL =
            SynchedEntityData.defineId(PoisonCloudEntity.class, EntityDataSerializers.BOOLEAN);

    private int     lifetime   = DEFAULT_LIFETIME;
    private float   tickDamage = 1.0f;
    private boolean loopSound  = false;

    public PoisonCloudEntity(EntityType<? extends PoisonCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setLifetime(int ticks)         { this.lifetime = ticks; }
    public void setTickDamage(float damage)    { this.tickDamage = damage; }
    public void setParticlesEnabled(boolean b) { this.entityData.set(DATA_PARTICLES, b); }
    public void setAcidPool(boolean b)         { this.entityData.set(DATA_ACID_POOL, b); }
    public void setLoopSound(boolean b)        { this.loopSound = b; }
    public void setDimensions(float width, float height) {
        this.entityData.set(DATA_WIDTH, width);
        this.entityData.set(DATA_HEIGHT, height);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_WIDTH, 12.0f);
        this.entityData.define(DATA_HEIGHT, 6.0f);
        this.entityData.define(DATA_PARTICLES, true);
        this.entityData.define(DATA_ACID_POOL, false);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(this.entityData.get(DATA_WIDTH), this.entityData.get(DATA_HEIGHT));
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_WIDTH.equals(key) || DATA_HEIGHT.equals(key)) this.refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= lifetime) {
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            if (this.entityData.get(DATA_PARTICLES)) spawnParticles();
            if (this.entityData.get(DATA_ACID_POOL)) spawnAcidSurface();
            return;
        }
        if (loopSound && (this.tickCount - 1) % PUDDLE_SOUND_INTERVAL == 0) {
            this.playSound(SoundsRegistry.PURPLE_WORM_POISON_PUDDLE.get(), 1.6F, 1.0F);
        }
        if (this.tickCount % POISON_INTERVAL == 0) {
            applyEffects();
        }
    }

    private void applyEffects() {
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            if (victim instanceof PurpleWormEntity) continue;
            if (tickDamage > 0.0f) victim.hurt(this.damageSources().magic(), tickDamage);
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0, false, false, true));
        }
    }

    private void spawnAcidSurface() {
        AABB box = this.getBoundingBox();
        for (int i = 0; i < ACID_BUBBLES_PER_TICK; i++) {
            if (this.random.nextFloat() > 0.55f) continue;
            double px = Mth.lerp(this.random.nextDouble(), box.minX, box.maxX);
            double pz = Mth.lerp(this.random.nextDouble(), box.minZ, box.maxZ);
            this.level().addParticle(ParticlesRegistry.ACID_BUBBLE.get(),
                    px, box.minY + 0.08, pz,
                    0.0, 0.015 + this.random.nextDouble() * 0.02, 0.0);
        }
    }

    private void spawnParticles() {
        AABB box = this.getBoundingBox();
        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double px = Mth.lerp(this.random.nextDouble(), box.minX, box.maxX);
            double py = Mth.lerp(this.random.nextDouble(), box.minY, box.maxY);
            double pz = Mth.lerp(this.random.nextDouble(), box.minZ, box.maxZ);
            this.level().addParticle(ParticlesRegistry.POISON_CLOUD.get(), px, py, pz, 0.0, 0.01, 0.0);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.contains("Lifetime")) this.lifetime = tag.getInt("Lifetime");
        if (tag.contains("TickDamage")) this.tickDamage = tag.getFloat("TickDamage");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("Lifetime", this.lifetime);
        tag.putFloat("TickDamage", this.tickDamage);
    }

    @Override public boolean isPickable()   { return false; }
    @Override public boolean isNoGravity()  { return true; }
    @Override public boolean isPushable()   { return false; }
    @Override protected boolean canRide(@NotNull Entity vehicle) { return false; }
}
