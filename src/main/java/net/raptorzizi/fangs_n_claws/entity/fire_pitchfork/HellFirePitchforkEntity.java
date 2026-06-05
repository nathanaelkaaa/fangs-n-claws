package net.raptorzizi.fangs_n_claws.entity.fire_pitchfork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.effect.HellFlamebrandEffect;
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
    protected void applyFlamebrandOnHit(LivingEntity victim) {
        HellFlamebrandEffect.addHellFlamebrandStack(victim);
    }

    @Override
    protected ItemStack getPickupItem() {
        ItemStack stack = super.getPickupItem();
        if (!stack.isEmpty() && !(stack.getItem() instanceof net.raptorzizi.fangs_n_claws.item.HellFirePitchforkItem)) {
            return new ItemStack(ItemsRegistry.HELLFIRE_PITCHFORK.get());
        }
        return stack;
    }
}
