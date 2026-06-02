package net.raptorzizi.fangs_n_claws.entity.dart_goblin;

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

    public PoisonousDartEntity(EntityType<? extends PoisonousDartEntity> type, Level level) {
        super(type, level);
        this.pickup = AbstractArrow.Pickup.ALLOWED;
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

        if (target instanceof LivingEntity living && target != owner) {
            living.hurt(this.damageSources().arrow(this, owner != null ? owner : this), 2.0f);
            if (!living.isDeadOrDying()) {
                int duration = owner instanceof DartGoblinEntity ? 100 : 200;
                living.addEffect(new MobEffectInstance(MobEffectsRegistry.VENOM, duration, 0));
            }
        }

        this.playSound(SoundsRegistry.DART_HIT.get(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.discard();
    }
}
