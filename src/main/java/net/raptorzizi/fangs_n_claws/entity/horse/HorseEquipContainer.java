package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

public class HorseEquipContainer implements Container {

    public static final int SADDLE = 0;
    public static final int ARMOR  = 1;
    public static final int SIZE   = 2;

    private final HorseMob horse;

    public HorseEquipContainer(HorseMob horse) {
        this.horse = horse;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return !horse.isSaddled() && horse.getArmor().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SADDLE -> horse.isSaddled() ? new ItemStack(ItemsRegistry.STURDY_SADDLE.get()) : ItemStack.EMPTY;
            case ARMOR  -> horse.getArmor();
            default     -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack current = getItem(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        setItem(slot, ItemStack.EMPTY);
        return current;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        switch (slot) {
            case SADDLE -> horse.setSturdySaddled(!stack.isEmpty());
            case ARMOR  -> horse.setArmor(stack);
            default     -> { }
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return horse.isAlive() && player.canInteractWithEntity(horse, 4.0);
    }

    @Override
    public void clearContent() {
        horse.setSturdySaddled(false);
        horse.setArmor(ItemStack.EMPTY);
    }
}
