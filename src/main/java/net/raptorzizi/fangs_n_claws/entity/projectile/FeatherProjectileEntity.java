package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class FeatherProjectileEntity extends AbstractArrow {

    public FeatherProjectileEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public FeatherProjectileEntity(Level level, LivingEntity shooter) {
        super(EntityRegistry.FEATHER_PROJECTILE.get(), shooter, level);
        this.pickup = Pickup.DISALLOWED;
        this.setBaseDamage(6.0);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ItemsRegistry.GIANT_FEATHER.get());
    }
}
