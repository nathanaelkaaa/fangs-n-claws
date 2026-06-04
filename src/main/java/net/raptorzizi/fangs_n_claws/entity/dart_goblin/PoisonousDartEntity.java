package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class PoisonousDartEntity extends AbstractArrow {

    private static final Field PIERCING_IGNORE_FIELD;
    static {
        try {
            PIERCING_IGNORE_FIELD = AbstractArrow.class.getDeclaredField("piercingIgnoreEntityIds");
            PIERCING_IGNORE_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(
                    "PoisonousDartEntity: cannot reflect AbstractArrow.piercingIgnoreEntityIds", e);
        }
    }

    public PoisonousDartEntity(EntityType<? extends PoisonousDartEntity> type, Level level) {
        super(type, level);
        this.pickup = Pickup.ALLOWED;
    }

    public PoisonousDartEntity(EntityType<? extends PoisonousDartEntity> type,
            double x, double y, double z, Level level,
            ItemStack pickupItem, @Nullable ItemStack firedFromWeapon) {
        super(type, x, y, z, level, pickupItem, firedFromWeapon);
        if (this.pickup != Pickup.CREATIVE_ONLY) {
            this.pickup = Pickup.ALLOWED;
        }
    }

    public void setCreativeOnlyPickup() {
        this.pickup = Pickup.CREATIVE_ONLY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.POISONOUS_DART.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundsRegistry.DART_HIT.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner  = this.getOwner();

        if (this.getPierceLevel() > 0) {
            try {
                @SuppressWarnings("unchecked")
                IntOpenHashSet ignoreSet = (IntOpenHashSet) PIERCING_IGNORE_FIELD.get(this);
                if (ignoreSet == null) {
                    ignoreSet = new IntOpenHashSet(5);
                    PIERCING_IGNORE_FIELD.set(this, ignoreSet);
                }
                if (ignoreSet.size() >= this.getPierceLevel() + 1) {
                    this.discard();
                    return;
                }
                ignoreSet.add(target.getId());
            } catch (Exception e) {
                this.discard();
                return;
            }
        }

        if (target instanceof LivingEntity living && target != owner) {
            living.hurt(this.damageSources().arrow(this, owner != null ? owner : this), 2.0f);
            if (!living.isDeadOrDying()) {
                int duration = owner instanceof DartGoblinEntity ? 100 : 200;
                living.addEffect(new MobEffectInstance(MobEffectsRegistry.VENOM, duration, 0));
            }
        }

        this.playSound(SoundsRegistry.DART_HIT.get(), 1.0F,
                1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

        if (this.getPierceLevel() <= 0) {
            this.discard();
        }
    }
}
