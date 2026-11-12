package net.raptorzizi.fangs_n_claws.item.amulet;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.raptorzizi.fangs_n_claws.registries.IngredientRegistry;

public record EmptyAmuletIngredient(Item item) implements ICustomIngredient {

    public static final MapCodec<EmptyAmuletIngredient> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec()
                            .fieldOf("item")
                            .forGetter(EmptyAmuletIngredient::item))
                    .apply(instance, EmptyAmuletIngredient::new));

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(this.item)
                && !AmuletItem.holdsFamiliar(stack)
                && !AmuletItem.isLinked(stack)
                && !AmuletItem.isDead(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(new ItemStack(this.item));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return IngredientRegistry.EMPTY_AMULET.get();
    }
}
