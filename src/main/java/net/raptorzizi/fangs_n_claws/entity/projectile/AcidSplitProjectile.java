package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.AcidCloudEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;

public class AcidSplitProjectile extends ThrowableProjectile {

    private static final int   MAX_LIFETIME  = 160;

    private static final float CLOUD_RADIUS       = 2.5f;
    private static final int   CLOUD_DURATION      = 60;
    private static final int   ACID_TICKS        = 100;
    private static final float SPLIT_CLOUD_DAMAGE  = 3.0f;

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(AcidSplitProjectile.class, EntityDataSerializers.FLOAT);

    private float damage = 8.0f;

    public AcidSplitProjectile(EntityType<? extends AcidSplitProjectile> type, Level level) {
        super(type, level);
    }

    public AcidSplitProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.ACID_SPLIT.get(), shooter, level);
    }

    public void setDamage(float damage) { this.damage = damage; }

    public void  setScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
        this.refreshDimensions();
    }
    public float getScale()            { return this.entityData.get(DATA_SCALE); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCALE, 1.0f);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return super.getDimensions(pose).scale(this.getScale());
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SCALE.equals(key)) this.refreshDimensions();
    }

    // Tick

    @Override
    public void tick() {
        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        super.tick();
        if (this.level().isClientSide) spawnAcidTrail();
    }

    private void spawnAcidTrail() {
        float scale = getScale();
        int count = 1 + (int) scale;
        Vec3 back = this.getDeltaMovement().scale(-0.25);
        for (int i = 0; i < count; i++) {
            double spread = 0.14 * scale;
            double ox = (this.random.nextDouble() * 2 - 1) * spread;
            double oy = (this.random.nextDouble() * 2 - 1) * spread;
            double oz = (this.random.nextDouble() * 2 - 1) * spread;
            this.level().addParticle(
                    ParticlesRegistry.ACID_TRAIL.get(),
                    this.getX() + ox, this.getY() + 0.1 + oy, this.getZ() + oz,
                    back.x * 0.5 + ox, back.y * 0.5 - 0.02, back.z * 0.5 + oz);
        }
    }

    // Impact

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) result);
        }

        if (!this.level().isClientSide) {
            this.playSound(SoundsRegistry.PURPLE_WORM_ACID_SPLASH.get(),
                    2.0F, 0.95F + this.random.nextFloat() * 0.1F);
            if (this.level() instanceof ServerLevel server) {
                spawnAcidCloud(server);
            }
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        Entity owner = this.getOwner();
        return owner == null || !entity.is(owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        Entity owner   = this.getOwner();
        if (owner != null && target.is(owner)) return;

        target.hurt(this.damageSources().thrown(this, owner), this.damage);
        if (target instanceof LivingEntity living && !living.isDeadOrDying()) {
            living.addEffect(new MobEffectInstance(MobEffectsRegistry.ACID, ACID_TICKS, 0, false, false, true));
        }
    }

    private void spawnAcidCloud(ServerLevel server) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.5, this.getZ());
        Vec3 to   = new Vec3(this.getX(), this.getY() - 4.0, this.getZ());
        BlockHitResult ground = server.clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        double groundY = (ground.getType() == HitResult.Type.BLOCK) ? ground.getLocation().y : this.getY();

        float scale = getScale();

        AcidCloudEntity cloud =
                new AcidCloudEntity(
                        EntityRegistry.ACID_CLOUD.get(), server);
        cloud.setPos(this.getX(), groundY, this.getZ());
        cloud.setDimensions(2.0f * CLOUD_RADIUS * scale, 2.0f);
        cloud.setParticlesEnabled(false);
        cloud.setAcidPool(true);
        cloud.setLoopSound(true);
        cloud.setLifetime(CLOUD_DURATION);
        cloud.setTickDamage(SPLIT_CLOUD_DAMAGE);
        server.addFreshEntity(cloud);


        double yJitter = this.random.nextDouble() * 0.05;
        server.sendParticles(ParticlesRegistry.ACID_SPLASH.get(),
                this.getX(), groundY + 0.05 + yJitter, this.getZ(), 0, scale, 0.0, 0.0, 1.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        tag.putFloat("Scale", getScale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        if (tag.contains("Scale"))  setScale(tag.getFloat("Scale"));
    }
}
