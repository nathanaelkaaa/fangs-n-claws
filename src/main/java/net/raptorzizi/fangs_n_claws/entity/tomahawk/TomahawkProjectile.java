package net.raptorzizi.fangs_n_claws.entity.tomahawk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

import javax.annotation.Nullable;

public class TomahawkProjectile extends AbstractArrow {

    private static final double RETURN_SPEED_FACTOR = 3.0;

    private float damage = 6.0f;
    private boolean dealtDamage;
    private int returnSoundTicks;

    public TomahawkProjectile(EntityType<? extends TomahawkProjectile> type, Level level) {
        super(type, level);
    }

    public TomahawkProjectile(Level level, LivingEntity thrower, ItemStack thrownStack) {
        super(EntityRegistry.TOMAHAWK_PROJECTILE.get(), thrower, level, thrownStack, null);
    }

    public TomahawkProjectile(Level level, double x, double y, double z, ItemStack thrownStack) {
        super(EntityRegistry.TOMAHAWK_PROJECTILE.get(), x, y, z, level, thrownStack, null);
    }

    public void setDamage(float damage) { this.damage = damage; }

    public boolean isInGround() { return this.inGround; }

    public ItemStack getItem() { return this.getPickupItemStackOrigin(); }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.TOMAHAWK.get());
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.WOOD_HIT;
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if ((this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 toOwner = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015 * RETURN_SPEED_FACTOR, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }
                double accel = 0.05 * RETURN_SPEED_FACTOR;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(toOwner.normalize().scale(accel)));
                if (this.returnSoundTicks == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.returnSoundTicks++;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) return false;
        return !(owner instanceof ServerPlayer) || !owner.isSpectator();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !target.is(this.getOwner());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        float f = this.damage;
        DamageSource source = this.damageSources().thrown(this, owner == null ? this : owner);
        if (this.level() instanceof ServerLevel serverLevel) {
            f = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), target, source, f);
        }

        boolean hurt = target.hurt(source, f);
        this.dealtDamage = true;

        if (hurt) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, source, this.getWeaponItem());
            }
            if (target instanceof LivingEntity living) {
                this.doKnockback(living, source);
                this.doPostHurtEffects(living);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS,
                1.0f, 0.9f + this.random.nextFloat() * 0.2f);

        if (hurt && this.wearOutAndBroke()) {
            this.discard();
            return;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
    }

    private boolean wearOutAndBroke() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return false;
        ItemStack stored = this.getPickupItemStackOrigin();
        if (!stored.isDamageableItem()) return false;

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        stored.hurtAndBreak(1, serverLevel, owner, item -> {});
        return stored.isEmpty();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player)
                || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void tickDespawn() {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        tag.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        this.dealtDamage = tag.getBoolean("DealtDamage");
    }
}
