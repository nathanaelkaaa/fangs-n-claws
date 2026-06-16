package net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork;

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

public class DecrepitPitchforkEntity extends ThrownTrident {

    // ── Instance ─────────────────────────────────────────────────────────────

    private ItemStack pitchforkStack = ItemStack.EMPTY;

    /** Deserialization constructor — called by the entity registry on world load. */
    public DecrepitPitchforkEntity(EntityType<? extends ThrownTrident> type, Level level) {
        super(type, level);
        this.pickup = Pickup.ALLOWED;
    }

    /** Player-throw constructor. */
    public DecrepitPitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, LivingEntity owner, ItemStack stack) {
        super(type, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.pickup = Pickup.ALLOWED;
        this.pitchforkStack = stack.copyWithCount(1);
        this.initFromStack(stack);
    }

    /** Dispenser / asProjectile constructor. */
    public DecrepitPitchforkEntity(EntityType<? extends ThrownTrident> type,
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

    // ── Hit ──────────────────────────────────────────────────────────────────

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity victim) {
            FlamebrandEffect.addFlamebrandStack(victim);
        }
    }

    // ── Item accessors ────────────────────────────────────────────────────────

    @Override
    protected ItemStack getPickupItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? new ItemStack(ItemsRegistry.DECREPIT_PITCHFORK.get())
                : this.pitchforkStack.copy();
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.pitchforkStack.isEmpty()) {
            // Overwrite "item" so ThrownTrident recomputes loyalty correctly on reload
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
