package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class FirePitchforkEntity extends ThrownTrident {

    private ItemStack pitchforkStack = ItemStack.EMPTY;

    public FirePitchforkEntity(EntityType<? extends ThrownTrident> type, Level level) {
        super(type, level);
        this.pickup = Pickup.ALLOWED;
    }

    public FirePitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, LivingEntity owner, ItemStack stack) {
        super(type, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.pickup = Pickup.ALLOWED;
        this.pitchforkStack = stack.copyWithCount(1);
        this.initFromStack(stack);
    }

    public FirePitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, double x, double y, double z, ItemStack stack) {
        super(type, level);
        this.setPos(x, y, z);
        this.pickup = Pickup.ALLOWED;
        this.pitchforkStack = stack.copyWithCount(1);
        this.initFromStack(stack);
    }

    private void initFromStack(ItemStack stack) {
        this.entityData.set(ThrownTrident.ID_FOIL, stack.hasFoil());
        byte loyalty = (byte) Math.min(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.LOYALTY, stack), 127);
        this.entityData.set(ThrownTrident.ID_LOYALTY, loyalty);
    }

    // Hit

    protected void applyFlamebrandOnHit(LivingEntity victim) {
        LivingEntity source = this.getOwner() instanceof LivingEntity le ? le : null;
        FlamebrandEffect.addFlamebrandStack(victim, source);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity victim) {
            applyFlamebrandOnHit(victim);
        }
    }

    // Item

    @Override
    protected ItemStack getPickupItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? new ItemStack(ItemsRegistry.FIRE_PITCHFORK.get())
                : this.pitchforkStack.copy();
    }

    // NBT

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.pitchforkStack.isEmpty()) {
            tag.put("item", this.pitchforkStack.save(new CompoundTag()));
            tag.put("PitchforkItem", this.pitchforkStack.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PitchforkItem")) {
            this.pitchforkStack = ItemStack.of(tag.getCompound("PitchforkItem"));
        }
    }
}
