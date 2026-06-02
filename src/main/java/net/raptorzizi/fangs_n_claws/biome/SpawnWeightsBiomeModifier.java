package net.raptorzizi.fangs_n_claws.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

import java.util.Set;

public record SpawnWeightsBiomeModifier() implements BiomeModifier {

    public static final MapCodec<SpawnWeightsBiomeModifier> CODEC =
            MapCodec.unit(new SpawnWeightsBiomeModifier());

    // Biome tags
    private static final TagKey<Biome> IS_OVERWORLD =
            TagKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("is_overworld"));
    private static final TagKey<Biome> IS_FOREST =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_forest"));
    private static final TagKey<Biome> IS_PLAINS =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_plains"));
    private static final ResourceKey<Biome> SOUL_SAND_VALLEY =
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("soul_sand_valley"));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {

        if (phase == Phase.REMOVE) {
            Set<EntityType<?>> ourMobs = Set.of(
                    EntityRegistry.GOBLIN.get(),
                    EntityRegistry.DART_GOBLIN.get(),
                    EntityRegistry.OGRE.get(),
                    EntityRegistry.GOLEM.get(),
                    EntityRegistry.OWLBEAR.get(),
                    EntityRegistry.SILVER_SKELETON.get(),
                    EntityRegistry.EVIL_BAT.get(),
                    EntityRegistry.GHOST.get(),
                    EntityRegistry.WEREWOLF.get()
            );
            MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
            for (MobCategory category : MobCategory.values()) {
                spawns.getSpawner(category).removeIf(sd -> ourMobs.contains(sd.type));
            }
            return;
        }

        if (phase == Phase.AFTER_EVERYTHING) {
            MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();

            if (biome.is(IS_OVERWORLD)) {
                if (CommonConfigs.ALLOW_SPAWN_GOBLIN.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.GOBLIN.get(),          ServerConfigs.GOBLIN_WEIGHT.get(),          1, 5);
                if (CommonConfigs.ALLOW_SPAWN_DART_GOBLIN.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.DART_GOBLIN.get(),     ServerConfigs.DART_GOBLIN_WEIGHT.get(),     1, 1);
                if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.OGRE.get(),            ServerConfigs.OGRE_WEIGHT.get(),            1, 2);
                if (CommonConfigs.ALLOW_SPAWN_CAVE_OGRE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.CAVE_OGRE.get(),       ServerConfigs.CAVE_OGRE_WEIGHT.get(),       1, 1);
                if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 2);
                if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                    add(spawns, MobCategory.AMBIENT,  EntityRegistry.EVIL_BAT.get(),        ServerConfigs.EVIL_BAT_WEIGHT.get(),        1, 5);
                if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.GHOST.get(),           ServerConfigs.GHOST_WEIGHT.get(),           1, 3);
                if (CommonConfigs.ALLOW_SPAWN_WEREWOLF.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.WEREWOLF.get(),        ServerConfigs.WEREWOLF_WEIGHT.get(),        1, 1);
            }

            if (biome.is(IS_PLAINS)) {
                if (CommonConfigs.ALLOW_SPAWN_GOLEM.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.GOLEM.get(),   ServerConfigs.GOLEM_WEIGHT.get(),   1, 1);
            }

            if (biome.is(IS_FOREST)) {
                if (CommonConfigs.ALLOW_SPAWN_OWLBEAR.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.OWLBEAR.get(), ServerConfigs.OWLBEAR_WEIGHT.get(), 1, 1);
            }

            if (biome.is(SOUL_SAND_VALLEY)) {
                if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_NETHER_WEIGHT.get(), 1, 3);
            }
        }
    }

    private static void add(MobSpawnSettingsBuilder builder, MobCategory category,
                             EntityType<?> type, int weight, int min, int max) {
        if (weight <= 0) return;
        builder.addSpawn(category, new MobSpawnSettings.SpawnerData(type, weight, min, max));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
