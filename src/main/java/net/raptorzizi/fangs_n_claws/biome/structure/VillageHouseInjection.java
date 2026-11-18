package net.raptorzizi.fangs_n_claws.biome.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class VillageHouseInjection {

    // Village pools
    private static final String[] BIOMES = { "plains", "desert", "savanna", "snowy", "taiga" };
    private static final String PIECE = "%s:village/%s/houses/%s_werevillager_house_";
    private static final int WEIGHT = 1;

    private static final Field TEMPLATES = field("templates");
    private static final Field RAW_TEMPLATES = field("rawTemplates");

    private VillageHouseInjection() { }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools =
                event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);

        for (String biome : BIOMES) {
            add(pools,
                    ResourceLocation.withDefaultNamespace("village/" + biome + "/houses"),
                    PIECE.formatted(FangsClawsMod.MOD_ID, biome, biome));
        }
    }

    private static void add(Registry<StructureTemplatePool> pools, ResourceLocation poolId, String piece) {
        StructureTemplatePool pool = pools.get(poolId);
        if (pool == null) {
            FangsClawsMod.LOGGER.warn("Village pool {} absent, maison de werevillager ignoree", poolId);
            return;
        }

        StructurePoolElement element = StructurePoolElement.legacy(piece)
                .apply(StructureTemplatePool.Projection.RIGID);

        try {
            @SuppressWarnings("unchecked")
            List<StructurePoolElement> templates = (List<StructurePoolElement>) TEMPLATES.get(pool);
            for (int copy = 0; copy < WEIGHT; copy++) {
                templates.add(element);
            }

            @SuppressWarnings("unchecked")
            List<Pair<StructurePoolElement, Integer>> raw =
                    (List<Pair<StructurePoolElement, Integer>>) RAW_TEMPLATES.get(pool);
            List<Pair<StructurePoolElement, Integer>> grown = new ArrayList<>(raw);
            grown.add(Pair.of(element, WEIGHT));
            RAW_TEMPLATES.set(pool, grown);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Injection impossible dans " + poolId, exception);
        }
    }

    private static Field field(String name) {
        try {
            Field found = StructureTemplatePool.class.getDeclaredField(name);
            found.setAccessible(true);
            return found;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("StructureTemplatePool." + name + " introuvable", exception);
        }
    }
}
