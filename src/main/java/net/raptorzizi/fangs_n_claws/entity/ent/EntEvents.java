package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class EntEvents {

    private EntEvents() { }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getState().is(BlockTags.LOGS)) return;
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;

        Player player = event.getPlayer();
        if (player.isCreative() || player.isSpectator()) return;
        if (player.hasEffect(MobEffectsRegistry.FOREST_BLESSING)) return;

        BlockPos pos = event.getPos();
        double radiusSqr = EntEntity.GUARD_RADIUS * EntEntity.GUARD_RADIUS;
        for (EntEntity ent : level.getEntitiesOfClass(EntEntity.class, new AABB(pos).inflate(EntEntity.GUARD_RADIUS))) {
            if (ent.distanceToSqr(pos.getCenter()) <= radiusSqr) ent.onLogBroken(player);
        }
    }
}
