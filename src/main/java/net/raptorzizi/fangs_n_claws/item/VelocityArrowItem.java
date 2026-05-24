package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.velocity_arrow.VelocityArrowEntity;

public class VelocityArrowItem extends ArrowItem {

    public VelocityArrowItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new VelocityArrowEntity(level, shooter, stack);
    }
}
