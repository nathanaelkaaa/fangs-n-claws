package net.raptorzizi.fangs_n_claws.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class DimensionSpawnCap {

    private DimensionSpawnCap() {}

    private static final Map<ResourceKey<Level>, Integer> COUNTS = new HashMap<>();

    public static boolean isOurMob(Entity entity) {
        return entity instanceof Mob
                && FangsClawsMod.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    public static boolean isOverCap(ServerLevel level) {
        var capValue = CommonConfigs.DIMENSION_CAPS.get(level.dimension().location());
        if (capValue == null) return false;
        int max = capValue.get();
        if (max < 0) return false;
        if (max == 0) return true;
        int threshold = max * level.getChunkSource().getLoadedChunksCount() / 289;
        int count = Math.max(0, COUNTS.getOrDefault(level.dimension(), 0));
        return count >= threshold;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!isOurMob(event.getEntity())) return;
        COUNTS.merge(event.getLevel().dimension(), 1, Integer::sum);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!isOurMob(event.getEntity())) return;
        COUNTS.merge(event.getLevel().dimension(), -1, Integer::sum);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        COUNTS.clear();
    }
}
