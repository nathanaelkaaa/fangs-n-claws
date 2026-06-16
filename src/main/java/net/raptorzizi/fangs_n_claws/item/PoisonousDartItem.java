package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartEntity;

public class PoisonousDartItem extends ArrowItem {

    public PoisonousDartItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        PoisonousDartEntity dart = new PoisonousDartEntity(level, shooter, stack);
        return dart;
    }
}
