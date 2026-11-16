package net.raptorzizi.fangs_n_claws.trade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.PurpleWormPitMapItem;
import net.raptorzizi.fangs_n_claws.registries.MapDecorationRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class VillagerTradeEvents {

    // Cartographer
    private static final int EXPERT_LEVEL = 4;
    private static final int EMERALD_COST = 42;
    private static final int MAX_USES = 12;
    private static final int VILLAGER_XP = 10;
    private static final String MAP_NAME = "filled_map.fangs_n_claws.purple_worm_pit";

    private static final int LISTING_COPIES = 9;

    private VillagerTradeEvents() { }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.CARTOGRAPHER) return;

        List<VillagerTrades.ItemListing> listings =
                event.getTrades().computeIfAbsent(EXPERT_LEVEL, level -> new ArrayList<>());

        for (int copy = 0; copy < LISTING_COPIES; copy++) {
            listings.add(new VillagerTrades.TreasureMapForEmeralds(
                    EMERALD_COST,
                    PurpleWormPitMapItem.PURPLE_WORM_PIT,
                    MAP_NAME,
                    MapDecorationRegistry.PURPLE_WORM_PIT.getDelegate(),
                    MAX_USES,
                    VILLAGER_XP));
        }
    }
}
