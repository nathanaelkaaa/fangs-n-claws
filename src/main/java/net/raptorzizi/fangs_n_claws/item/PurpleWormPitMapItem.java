package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MapDecorationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PurpleWormPitMapItem extends Item {

    public static final TagKey<Structure> PURPLE_WORM_PIT =
            TagKey.create(Registries.STRUCTURE, FangsClawsMod.id("purple_worm_pit"));

    // Search
    private static final int SEARCH_RADIUS_CHUNKS = 150;
    private static final boolean SKIP_EXISTING_CHUNKS = true;

    // Map
    private static final byte MAP_SCALE = 2;
    private static final String MARKER = "+";

    public PurpleWormPitMapItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,
                                                          @NotNull Player player,
                                                          @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        ItemStack map = create(serverLevel, player.blockPosition());
        if (map == null) {
            player.displayClientMessage(
                    Component.translatable("item.fangs_n_claws.purple_worm_pit_map.not_found"), true);
            return InteractionResultHolder.fail(stack);
        }

        player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
        stack.shrink(1);

        if (stack.isEmpty()) {
            return InteractionResultHolder.sidedSuccess(map, false);
        }
        if (!player.getInventory().add(map)) {
            player.drop(map, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Nullable
    public static ItemStack create(ServerLevel level, BlockPos origin) {
        BlockPos pit = level.findNearestMapStructure(
                PURPLE_WORM_PIT, origin, SEARCH_RADIUS_CHUNKS, SKIP_EXISTING_CHUNKS);
        if (pit == null) return null;

        ItemStack map = MapItem.create(level, pit.getX(), pit.getZ(), MAP_SCALE, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, pit, MARKER,
                MapDecorationRegistry.PURPLE_WORM_PIT.getDelegate());
        map.set(DataComponents.ITEM_NAME,
                Component.translatable("filled_map.fangs_n_claws.purple_worm_pit"));

        return map;
    }
}
