package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.raptorzizi.fangs_n_claws.effect.HellFlamebrandEffect;
import net.raptorzizi.fangs_n_claws.item.HellFirePitchforkItem;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class HellFirePitchforkEntity extends FirePitchforkEntity {

    public HellFirePitchforkEntity(EntityType<? extends ThrownTrident> type, Level level) {
        super(type, level);
    }

    public HellFirePitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, LivingEntity owner, ItemStack stack) {
        super(type, level, owner, stack);
    }

    public HellFirePitchforkEntity(EntityType<? extends ThrownTrident> type,
            Level level, double x, double y, double z, ItemStack stack) {
        super(type, level, x, y, z, stack);
    }

    @Override
    public float getBaseThrownDamage() {
        return 7.0f;
    }

    @Override
    protected void applyFlamebrandOnHit(LivingEntity victim) {
        LivingEntity source = this.getOwner() instanceof LivingEntity le ? le : null;
        HellFlamebrandEffect.addHellFlamebrandStack(victim, source);
    }

    @Override
    protected ItemStack getPickupItem() {
        ItemStack stack = super.getPickupItem();
        if (!stack.isEmpty() && !(stack.getItem() instanceof HellFirePitchforkItem)) {
            return new ItemStack(ItemsRegistry.HELLFIRE_PITCHFORK.get());
        }
        return stack;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.HELLFIRE_PITCHFORK.get());
    }
}
