package net.raptorzizi.fangs_n_claws.trade;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.raptorzizi.fangs_n_claws.item.PurpleWormPitMapItem;
import org.jetbrains.annotations.Nullable;

public class PurpleWormPitMapForEmeralds implements VillagerTrades.ItemListing {

    // Rarity
    private static final float CHANCE = 0.1F;

    // Price
    private static final int EMERALD_COST = 24;
    private static final int MAX_USES = 1;
    private static final int VILLAGER_XP = 30;
    private static final float PRICE_MULTIPLIER = 0.2F;

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (!(trader.level() instanceof ServerLevel serverLevel)) return null;
        if (random.nextFloat() >= CHANCE) return null;

        ItemStack map = PurpleWormPitMapItem.create(serverLevel, trader.blockPosition());
        if (map == null) return null;

        return new MerchantOffer(
                new ItemCost(Items.EMERALD, EMERALD_COST),
                Optional.of(new ItemCost(Items.COMPASS)),
                map,
                MAX_USES,
                VILLAGER_XP,
                PRICE_MULTIPLIER);
    }
}
