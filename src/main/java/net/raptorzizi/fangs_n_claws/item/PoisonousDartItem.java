package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.Nullable;

public class PoisonousDartItem extends ArrowItem {

    public PoisonousDartItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack,
                                     LivingEntity shooter, @Nullable ItemStack weapon) {
        return new PoisonousDartEntity(level, shooter, stack, weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        PoisonousDartEntity dart = new PoisonousDartEntity(
                EntityRegistry.POISONOUS_DART.get(),
                pos.x(), pos.y(), pos.z(),
                level, stack.copyWithCount(1), null);
        dart.pickup = AbstractArrow.Pickup.ALLOWED;
        return dart;
    }
}
