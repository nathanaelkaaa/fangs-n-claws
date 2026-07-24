package net.raptorzizi.fangs_n_claws.entity.tomahawk;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class TomahawkDispenseBehavior extends DefaultDispenseItemBehavior {

    private static final float THROW_DAMAGE = 6.0f;
    private static final float POWER        = 1.1f;
    private static final float UNCERTAINTY  = 6.0f;

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction dir = source.state().getValue(DispenserBlock.FACING);
        Position pos = DispenserBlock.getDispensePosition(source);

        ItemStack thrown = stack.copy();
        thrown.setCount(1);

        TomahawkProjectile tomahawk = new TomahawkProjectile(level, pos.x(), pos.y(), pos.z(), thrown);
        tomahawk.setDamage(THROW_DAMAGE);
        tomahawk.pickup = AbstractArrow.Pickup.ALLOWED;
        tomahawk.shoot(dir.getStepX(), dir.getStepY() + 0.1F, dir.getStepZ(), POWER, UNCERTAINTY);
        level.addFreshEntity(tomahawk);

        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
