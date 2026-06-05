package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

import java.lang.reflect.Field;

public class FirePitchforkEntity extends ThrownTrident {

    private static final EntityDataAccessor<Byte>    ACCESSOR_LOYALTY;
    private static final EntityDataAccessor<Boolean> ACCESSOR_FOIL;

    static {
        ACCESSOR_LOYALTY = reflectStaticField("ID_LOYALTY");
        ACCESSOR_FOIL    = reflectStaticField("ID_FOIL");
    }

    @SuppressWarnings("unchecked")
    private static <T> EntityDataAccessor<T> reflectStaticField(String name) {
        try {
            Field f = ThrownTrident.class.getDeclaredField(name);
            f.setAccessible(true);
            return (EntityDataAccessor<T>) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("FirePitchforkEntity: cannot reflect ThrownTrident." + name, e);
        }
    }

    // Instance

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
        this.initFromStack(stack, level);
    }

    public FirePitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, double x, double y, double z, ItemStack stack) {
        super(type, level);
        this.setPos(x, y, z);
        this.pickup = Pickup.ALLOWED;
        this.pitchforkStack = stack.copyWithCount(1);
        this.initFromStack(stack, level);
    }

    private void initFromStack(ItemStack stack, Level level) {
        this.setPickupItemStack(this.pitchforkStack);
        this.entityData.set(ACCESSOR_FOIL, stack.hasFoil());
        if (level instanceof ServerLevel serverLevel) {
            byte loyalty = (byte) Mth.clamp(
                    EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this),
                    0, 127);
            this.entityData.set(ACCESSOR_LOYALTY, loyalty);
        }
    }

    // Hit

    public float getBaseThrownDamage() {
        return 5.0f;
    }

    protected void applyFlamebrandOnHit(LivingEntity victim) {
        FlamebrandEffect.addFlamebrandStack(victim);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity victim) {
            applyFlamebrandOnHit(victim);
        }
    }

    // Getter

    @Override
    protected ItemStack getPickupItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? new ItemStack(ItemsRegistry.FIRE_PITCHFORK.get())
                : this.pitchforkStack.copy();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.FIRE_PITCHFORK.get());
    }

    @Override
    public ItemStack getWeaponItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? super.getWeaponItem()
                : this.pitchforkStack;
    }

    // NBT

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.pitchforkStack.isEmpty()) {
            tag.put("item", this.pitchforkStack.save(this.registryAccess()));
            tag.put("PitchforkItem", this.pitchforkStack.save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PitchforkItem", Tag.TAG_COMPOUND)) {
            this.pitchforkStack = ItemStack.parse(
                    this.registryAccess(), tag.getCompound("PitchforkItem"))
                    .orElse(ItemStack.EMPTY);
        }
    }
}
