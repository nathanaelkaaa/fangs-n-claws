package net.raptorzizi.fangs_n_claws.entity.tomahawk;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class TomahawkDispenseBehavior extends DefaultDispenseItemBehavior {

    private static final float THROW_DAMAGE = 6.0f;
    private static final float POWER        = 1.1f;
    private static final float UNCERTAINTY  = 6.0f;

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.getLevel();
        Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
        Position pos = DispenserBlock.getDispensePosition(source);

        TomahawkProjectile tomahawk = new TomahawkProjectile(EntityRegistry.TOMAHAWK_PROJECTILE.get(), level);
        tomahawk.setPos(pos.x(), pos.y(), pos.z());
        tomahawk.setDamage(THROW_DAMAGE);
        tomahawk.shoot(dir.getStepX(), dir.getStepY() + 0.1F, dir.getStepZ(), POWER, UNCERTAINTY);
        level.addFreshEntity(tomahawk);

        int nextDamage = stack.getDamageValue() + 1;
        if (nextDamage >= stack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        stack.setDamageValue(nextDamage);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.getLevel().levelEvent(1002, source.getPos(), 0);
    }
}
