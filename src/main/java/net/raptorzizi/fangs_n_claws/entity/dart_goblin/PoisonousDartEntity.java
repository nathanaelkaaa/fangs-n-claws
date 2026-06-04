package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class PoisonousDartEntity extends AbstractArrow {

    /** Tracks entity IDs already pierced so the dart skips them on subsequent ticks. */
    @org.jetbrains.annotations.Nullable
    private IntOpenHashSet piercedIds;

    public PoisonousDartEntity(EntityType<? extends PoisonousDartEntity> type, Level level) {
        super(type, level);
        this.pickup = AbstractArrow.Pickup.ALLOWED;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (piercedIds != null && piercedIds.contains(entity.getId())) return false;
        return super.canHitEntity(entity);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ItemsRegistry.POISONOUS_DART.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner  = this.getOwner();

        if (target instanceof LivingEntity living && target != owner) {
            living.hurt(this.level().damageSources().arrow(this, owner != null ? owner : this), 2.0f);
            if (!living.isDeadOrDying()) {
                int duration = owner instanceof DartGoblinEntity ? 100 : 200;
                living.addEffect(new MobEffectInstance(MobEffectsRegistry.VENOM.get(), duration, 0));
            }
        }

        this.playSound(SoundsRegistry.DART_HIT.get(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

        // Respect Piercing enchantment: track hit entities, only discard when charges exhausted
        byte pierceLevel = this.getPierceLevel();
        if (pierceLevel > 0) {
            if (this.piercedIds == null) {
                this.piercedIds = new IntOpenHashSet(5);
            }
            this.piercedIds.add(target.getId());
            if (this.piercedIds.size() >= pierceLevel + 1) {
                this.discard();
            }
        } else {
            this.discard();
        }
    }
}
