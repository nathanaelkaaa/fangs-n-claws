package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class VileFatItem extends Item {

    private static final int BURN_TIME = 5000;

    public VileFatItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }
}
