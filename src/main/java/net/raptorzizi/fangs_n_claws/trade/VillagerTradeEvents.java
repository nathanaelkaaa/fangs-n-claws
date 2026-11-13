package net.raptorzizi.fangs_n_claws.trade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class VillagerTradeEvents {

    private static final int[] LEVELS = {4, 5};

    private VillagerTradeEvents() { }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.CARTOGRAPHER) return;

        for (int level : LEVELS) {
            List<VillagerTrades.ItemListing> listings =
                    event.getTrades().computeIfAbsent(level, key -> new ArrayList<>());
            listings.add(new PurpleWormPitMapForEmeralds());
        }
    }
}
