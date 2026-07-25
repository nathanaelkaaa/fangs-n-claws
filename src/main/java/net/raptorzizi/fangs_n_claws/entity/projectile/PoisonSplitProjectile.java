package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class PoisonSplitProjectile extends ThrowableProjectile {

    private static final int   MAX_LIFETIME  = 100;
    private static final byte  EVENT_SPLASH  = 60;

    private static final float CLOUD_RADIUS   = 2.5f;
    private static final int   CLOUD_DURATION = 120;
    private static final int   POISON_TICKS   = 100;

    private float damage = 4.0f;

    public PoisonSplitProjectile(EntityType<? extends PoisonSplitProjectile> type, Level level) {
        super(type, level);
    }

    public PoisonSplitProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.POISON_SPLIT.get(), shooter, level);
    }

    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // Tick

    @Override
    public void tick() {
        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        super.tick();

        if (this.level().isClientSide && !this.onGround()) {
            Vec3 delta = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SLIME,
                        this.getX() + delta.x * i / 4.0,
                        this.getY() + delta.y * i / 4.0,
                        this.getZ() + delta.z * i / 4.0,
                        -delta.x, -delta.y + 0.2, -delta.z);
            }
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
            this.level().broadcastEntityEvent(this, EVENT_SPLASH);
            this.playSound(SoundEvents.SLIME_SQUISH, 1.0F,
                    0.9F + this.random.nextFloat() * 0.2F);
            if (this.level() instanceof ServerLevel server) {
                spawnPoisonCloud(server);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        Entity owner   = this.getOwner();
        if (target == owner) return;

        target.hurt(this.damageSources().thrown(this, owner), this.damage);
        if (target instanceof LivingEntity living && !living.isDeadOrDying()) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0));
        }
    }

    private void spawnPoisonCloud(ServerLevel server) {
        AreaEffectCloud cloud = new AreaEffectCloud(server, this.getX(), this.getY(), this.getZ());
        if (this.getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setDuration(CLOUD_DURATION);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setRadiusPerTick(-CLOUD_RADIUS / (float) CLOUD_DURATION);
        cloud.setParticle(ParticleTypes.ITEM_SLIME);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        server.addFreshEntity(cloud);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SPLASH) {
            for (int i = 0; i < 16; i++) {
                double vx = (this.random.nextDouble() - 0.5) * 0.9;
                double vy =  this.random.nextDouble() * 0.8;
                double vz = (this.random.nextDouble() - 0.5) * 0.9;
                this.level().addParticle(ParticleTypes.ITEM_SLIME,
                        this.getX(), this.getY() + 0.1, this.getZ(), vx, vy, vz);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
    }
}
