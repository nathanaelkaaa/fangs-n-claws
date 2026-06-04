package net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork;

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

public class DecrepitPitchforkEntity extends ThrownTrident {

    /**
     * Reflect the private static entity-data accessors from ThrownTrident so we can
     * set ID_LOYALTY and ID_FOIL correctly without calling the vanilla 3-arg constructor
     * (which hard-codes EntityType.TRIDENT).
     */
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
            throw new RuntimeException("DecrepitPitchforkEntity: cannot reflect ThrownTrident." + name, e);
        }
    }

    // ── Instance ──────────────────────────────────────────────────────────────

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
        this.initFromStack(stack, level);
    }

    /** Dispenser / asProjectile constructor. */
    public DecrepitPitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, double x, double y, double z, ItemStack stack) {
        super(type, level);
        this.setPos(x, y, z);
        this.pickup = Pickup.ALLOWED;
        this.pitchforkStack = stack.copyWithCount(1);
        this.initFromStack(stack, level);
    }

    /**
     * Copies the item into AbstractArrow's private pickupItemStack field (via the
     * protected setter), and initialises the synced ID_LOYALTY / ID_FOIL data so
     * the Loyalty return mechanic and enchantment glint work from the very first tick.
     */
    private void initFromStack(ItemStack stack, Level level) {
        // setPickupItemStack is protected in AbstractArrow — no reflection needed.
        this.setPickupItemStack(this.pitchforkStack);

        this.entityData.set(ACCESSOR_FOIL, stack.hasFoil());

        if (level instanceof ServerLevel serverLevel) {
            byte loyalty = (byte) Mth.clamp(
                    EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this),
                    0, 127);
            this.entityData.set(ACCESSOR_LOYALTY, loyalty);
        }
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

    /**
     * Called by AbstractArrow.tryPickup → what the player actually receives.
     * We bypass AbstractArrow's cached "pickupItemStack" field (which is set from
     * getDefaultPickupItem() during super() before pitchforkStack is initialised).
     */
    @Override
    protected ItemStack getPickupItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? new ItemStack(ItemsRegistry.DECREPIT_PITCHFORK.get())
                : this.pitchforkStack.copy();
    }

    /** Fallback used by AbstractArrow to initialise its pickupItemStack field. */
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.DECREPIT_PITCHFORK.get());
    }

    /** Used for damage / enchantment-effect calculations when the trident hits. */
    @Override
    public ItemStack getWeaponItem() {
        return (this.pitchforkStack == null || this.pitchforkStack.isEmpty())
                ? super.getWeaponItem()
                : this.pitchforkStack;
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); // AbstractArrow writes pickupItemStack under "item"
        if (!this.pitchforkStack.isEmpty()) {
            // "item" is the key AbstractArrow reads back in readAdditionalSaveData →
            // setPickupItemStack → getPickupItemStackOrigin → used by ThrownTrident to
            // recompute ID_LOYALTY on reload. Overwrite it with our full stack.
            tag.put("item", this.pitchforkStack.save(this.registryAccess()));
            tag.put("PitchforkItem", this.pitchforkStack.save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        // super chain: AbstractArrow reads "item" → setPickupItemStack(enchanted stack)
        //              ThrownTrident reads "DealtDamage" and recomputes ID_LOYALTY from
        //              getPickupItemStackOrigin() — which is now our enchanted pitchfork.
        super.readAdditionalSaveData(tag);
        if (tag.contains("PitchforkItem", Tag.TAG_COMPOUND)) {
            this.pitchforkStack = ItemStack.parse(
                    this.registryAccess(), tag.getCompound("PitchforkItem"))
                    .orElse(ItemStack.EMPTY);
        }
    }
}
