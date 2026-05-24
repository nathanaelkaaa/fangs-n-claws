package net.raptorzizi.fangs_n_claws.entity.evil_eye;

import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class EvilEyeProjectile extends ThrowableItemProjectile {

    private static final int   LIFETIME_TICKS    = 60;
    private static final float EFFECT_RADIUS     = 4.0f;
    private static final int   BLINDNESS_DURATION = 100;

    public EvilEyeProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public EvilEyeProjectile(Level level, LivingEntity thrower) {
        super(EntityRegistry.EVIL_EYE_PROJECTILE.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ItemsRegistry.EVIL_EYE.get();
    }

    @Override
    protected float getGravity() {
        return 0.04f;
    }

    private static final float PROXIMITY_RADIUS = 2.0f;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (this.tickCount >= LIFETIME_TICKS) {
            explode();
            this.discard();
            return;
        }

        AABB proximity = this.getBoundingBox().inflate(PROXIMITY_RADIUS);
        boolean nearFlyer = !this.level().getEntitiesOfClass(LivingEntity.class, proximity,
                e -> e != this.getOwner() && (e instanceof Phantom || e instanceof EvilBatEntity))
                .isEmpty();

        if (nearFlyer) {
            explode();
            this.discard();
        }
    }

    // Collision

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        explode();
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) return;
        explode();
        this.discard();
    }

    private void explode() {
        AABB area = this.getBoundingBox().inflate(EFFECT_RADIUS);
        DamageSource source = this.level().damageSources().thrown(this, this.getOwner());
        this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e.distanceTo(this) <= EFFECT_RADIUS && e != this.getOwner())
            .forEach(e -> {
                if (e instanceof Phantom || e instanceof EvilBatEntity) {
                    e.hurt(source, Float.MAX_VALUE);
                } else {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, 0));
                }
            });

        this.level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_EYE_DEATH, SoundSource.NEUTRAL,
                1.2f, 0.8f + random.nextFloat() * 0.4f);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticlesRegistry.BLACK_FOG.get(),
                    getX(), getY() + 0.5, getZ(), 12, 0.3, 0.3, 0.3, 0.25);
            sl.sendParticles(ParticlesRegistry.BLACK_FOG.get(),
                    getX(), getY() + 0.5, getZ(), 10, 1.8, 0.6, 1.8, 0.05);
        }
    }
}
