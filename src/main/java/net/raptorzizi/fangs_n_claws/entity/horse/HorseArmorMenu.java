package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MenuTypeRegistry;

public class HorseArmorMenu extends AbstractContainerMenu {

    private static final int INV_START = HorseEquipContainer.SIZE;

    private final Container container;
    private final HorseMob  horse;

    public HorseArmorMenu(int containerId, Inventory playerInv, HorseMob horse) {
        super(MenuTypeRegistry.HORSE_ARMOR.get(), containerId);
        this.horse = horse;
        this.container = new HorseEquipContainer(horse);
        this.container.startOpen(playerInv.player);

        // Slot selle (0)
        this.addSlot(new Slot(this.container, HorseEquipContainer.SADDLE, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemsRegistry.STURDY_SADDLE.get()) && !this.hasItem();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(this.container, HorseEquipContainer.ARMOR, 8, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return horse.isBodyArmorItem(stack) && !this.hasItem();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 102 + row * 18 - 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public static HorseArmorMenu fromNetwork(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        Entity entity = playerInv.player.level().getEntity(buf.readInt());
        return new HorseArmorMenu(containerId, playerInv, (HorseMob) entity);
    }

    public HorseMob getHorse() {
        return this.horse;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player) && this.horse.isAlive() && this.horse.isTamed()
                && player.distanceToSqr(this.horse) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < INV_START) {
                if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (this.getSlot(HorseEquipContainer.ARMOR).mayPlace(stack)
                    && !this.getSlot(HorseEquipContainer.ARMOR).hasItem()) {
                if (!this.moveItemStackTo(stack, HorseEquipContainer.ARMOR, HorseEquipContainer.ARMOR + 1, false)) return ItemStack.EMPTY;
            } else if (this.getSlot(HorseEquipContainer.SADDLE).mayPlace(stack)
                    && !this.getSlot(HorseEquipContainer.SADDLE).hasItem()) {
                if (!this.moveItemStackTo(stack, HorseEquipContainer.SADDLE, HorseEquipContainer.SADDLE + 1, false)) return ItemStack.EMPTY;
            } else {
                int mainStart = INV_START;
                int mainEnd   = INV_START + 27;
                int hotbarEnd = mainEnd + 9;
                if (index >= mainStart && index < mainEnd) {
                    if (!this.moveItemStackTo(stack, mainEnd, hotbarEnd, false)) return ItemStack.EMPTY;
                } else if (index >= mainEnd && index < hotbarEnd) {
                    if (!this.moveItemStackTo(stack, mainStart, mainEnd, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
