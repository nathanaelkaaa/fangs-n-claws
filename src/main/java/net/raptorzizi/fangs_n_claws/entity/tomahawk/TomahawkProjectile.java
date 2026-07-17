package net.raptorzizi.fangs_n_claws.entity.tomahawk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class TomahawkProjectile extends ThrowableItemProjectile {

    private static final int LIFETIME_TICKS = 80;
    private float damage = 6.0f;

    public TomahawkProjectile(EntityType<? extends TomahawkProjectile> type, Level level) {
        super(type, level);
    }

    public TomahawkProjectile(Level level, LivingEntity thrower) {
        super(EntityRegistry.TOMAHAWK_PROJECTILE.get(), thrower, level);
    }

    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected Item getDefaultItem() {
        return ItemsRegistry.TOMAHAWK.get();
    }

    @Override
    protected float getGravity() {
        return 0.05f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        DamageSource source = this.damageSources().thrown(this, this.getOwner());
        target.hurt(source, this.damage);

        this.level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS,
                1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;

        this.level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.WOOD_HIT, SoundSource.BLOCKS,
                0.9f, 0.9f + this.random.nextFloat() * 0.2f);
        this.discard();
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
