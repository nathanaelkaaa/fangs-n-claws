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
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreEntity;

import java.util.Set;

public record SpawnWeightsBiomeModifier() implements BiomeModifier {

    public static final MapCodec<SpawnWeightsBiomeModifier> CODEC =
            MapCodec.unit(new SpawnWeightsBiomeModifier());

    // Biome tags
    private static final TagKey<Biome> NO_NATURAL_SPAWN =
            TagKey.create(Registries.BIOME, FangsClawsMod.id("no_natural_spawn"));
    private static final TagKey<Biome> IS_OVERWORLD =
            TagKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("is_overworld"));
    private static final TagKey<Biome> IS_FOREST =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_forest"));
    private static final TagKey<Biome> IS_PLAINS =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_plains"));
    private static final ResourceKey<Biome> CRIMSON_FOREST =
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("crimson_forest"));
    private static final ResourceKey<Biome> NETHER_WASTES =
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("nether_wastes"));
    private static final ResourceKey<Biome> SOUL_SAND_VALLEY =
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("soul_sand_valley"));
    private static final TagKey<Biome> IS_NETHER =
            TagKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("is_nether"));
    private static final ResourceKey<Biome> SNOWY_PLAINS =
            ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("snowy_plains"));
    private static final TagKey<Biome> IS_DESERT =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_desert"));
    private static final TagKey<Biome> IS_SNOWY =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_snowy"));
    private static final TagKey<Biome> IS_JUNGLE =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_jungle"));
    private static final TagKey<Biome> IS_TAIGA =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_taiga"));
    private static final TagKey<Biome> IS_SAVANNA =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_savanna"));
    private static final TagKey<Biome> IS_BADLANDS =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_badlands"));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {

        if (phase == Phase.REMOVE) {
            Set<EntityType<?>> ourMobs = Set.of(
                    EntityRegistry.GOBLIN.get(),
                    EntityRegistry.DART_GOBLIN.get(),
                    EntityRegistry.IMP.get(),
                    EntityRegistry.OGRE.get(),
                    EntityRegistry.CAVE_OGRE.get(),
                    EntityRegistry.GOLEM.get(),
                    EntityRegistry.OWLBEAR.get(),
                    EntityRegistry.SHRIKE.get(),
                    EntityRegistry.SILVER_SKELETON.get(),
                    EntityRegistry.EVIL_BAT.get(),
                    EntityRegistry.GHOST.get(),
                    EntityRegistry.FIRE_GHOST.get(),
                    EntityRegistry.WEREWOLF.get(),
                    EntityRegistry.HELL_OGRE.get(),
                    EntityRegistry.SCORPION.get(),
                    EntityRegistry.DESERT_SCORPION.get(),
                    EntityRegistry.FROST_SCORPION.get(),
                    EntityRegistry.NETHER_SCORPION.get(),
                    EntityRegistry.ICE_GOLEM.get(),
                    EntityRegistry.HORSE_BAT.get(),
                    EntityRegistry.NIGHTMARE_HORSE.get(),
                    EntityRegistry.SKELETON_HORSE_MOB.get(),
                    EntityRegistry.ZOMBIE_HORSE_MOB.get(),
                    EntityRegistry.WILD_WOLF.get()
            );
            MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
            for (MobCategory category : MobCategory.values()) {
                spawns.getSpawner(category).removeIf(sd -> ourMobs.contains(sd.type));
            }
            return;
        }

        if (phase == Phase.AFTER_EVERYTHING) {
            // Biomes explicitement exclus (Mushroom Fields, Deep Dark...) : on n'y ajoute rien,
            // quoi qu'aient fait les autres mods avant nous.
            if (biome.is(NO_NATURAL_SPAWN)) return;

            MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();

            if (biome.is(IS_OVERWORLD) && !spawns.getSpawner(MobCategory.MONSTER).isEmpty()) {
                if (CommonConfigs.ALLOW_SPAWN_GOBLIN.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.GOBLIN.get(),          ServerConfigs.GOBLIN_WEIGHT.get(),          1, 4);
                if (CommonConfigs.ALLOW_SPAWN_DART_GOBLIN.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.DART_GOBLIN.get(),     ServerConfigs.DART_GOBLIN_WEIGHT.get(),     1, 1);
                if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.OGRE.get(),            ServerConfigs.OGRE_WEIGHT.get(),            1, 1);
                if (CommonConfigs.ALLOW_SPAWN_CAVE_OGRE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.CAVE_OGRE.get(),       ServerConfigs.CAVE_OGRE_WEIGHT.get(),       1, 1);
                if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
                if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.EVIL_BAT.get(),        ServerConfigs.EVIL_BAT_WEIGHT.get(),        1, 3);
                if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.GHOST.get(),           ServerConfigs.GHOST_WEIGHT.get(),           1, 2);
                if (CommonConfigs.ALLOW_SPAWN_FIRE_GHOST.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.FIRE_GHOST.get(),      ServerConfigs.FIRE_GHOST_WEIGHT.get(),      1, 1);
                if (CommonConfigs.ALLOW_SPAWN_HORSE_BAT.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.HORSE_BAT.get(),       ServerConfigs.HORSE_BAT_WEIGHT.get(),       1, 1);
                if (CommonConfigs.ALLOW_NATURAL_SPAWN_SKELETON_HORSE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.SKELETON_HORSE_MOB.get(), ServerConfigs.SKELETON_HORSE_WEIGHT.get(), 1, 1);
                if (CommonConfigs.ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.ZOMBIE_HORSE_MOB.get(),   ServerConfigs.ZOMBIE_HORSE_WEIGHT.get(),   1, 1);
                if (CommonConfigs.ALLOW_SPAWN_WILD_WOLF.get()
                        && !biome.is(IS_DESERT) && !biome.is(IS_JUNGLE)
                        && !biome.is(IS_SAVANNA) && !biome.is(IS_BADLANDS))
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.WILD_WOLF.get(),       ServerConfigs.WILD_WOLF_WEIGHT.get(),       1, 4);
                if (CommonConfigs.ALLOW_SPAWN_WEREWOLF.get())
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.WEREWOLF.get(),        ServerConfigs.WEREWOLF_WEIGHT.get(),        1, 1);
                if (CommonConfigs.ALLOW_SPAWN_SCORPION.get() && !biome.is(IS_DESERT) && !biome.is(IS_SNOWY))
                    add(spawns, MobCategory.MONSTER,  EntityRegistry.SCORPION.get(),        ServerConfigs.SCORPION_WEIGHT.get(),        1, 1);
            }

            if (biome.is(SNOWY_PLAINS)) {
                if (CommonConfigs.ALLOW_SPAWN_ICE_GOLEM.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.ICE_GOLEM.get(), ServerConfigs.ICE_GOLEM_WEIGHT.get(), 1, 1);
            }

            if (biome.is(IS_SNOWY)) {
                if (CommonConfigs.ALLOW_SPAWN_FROST_SCORPION.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.FROST_SCORPION.get(), ServerConfigs.FROST_SCORPION_WEIGHT.get(), 1, 2);
            }

            if (biome.is(IS_DESERT)) {
                if (CommonConfigs.ALLOW_SPAWN_DESERT_SCORPION.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.DESERT_SCORPION.get(), ServerConfigs.DESERT_SCORPION_WEIGHT.get(), 1, 2);
            }

            if (biome.is(IS_PLAINS) && !biome.is(SNOWY_PLAINS)) {
                if (CommonConfigs.ALLOW_SPAWN_GOLEM.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.GOLEM.get(),   ServerConfigs.GOLEM_WEIGHT.get(),   1, 1);
            }

            if (biome.is(IS_FOREST) || biome.is(IS_TAIGA)) {
                if (CommonConfigs.ALLOW_SPAWN_OWLBEAR.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.OWLBEAR.get(), ServerConfigs.OWLBEAR_WEIGHT.get(), 1, 1);
            }

            if (biome.is(IS_SNOWY) && (biome.is(IS_FOREST) || biome.is(IS_TAIGA))) {
                if (CommonConfigs.ALLOW_SPAWN_SHRIKE.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.SHRIKE.get(), ServerConfigs.SHRIKE_WEIGHT.get(), 1, 1);
            }

            if (biome.is(CRIMSON_FOREST) || biome.is(NETHER_WASTES)) {
                if (CommonConfigs.ALLOW_SPAWN_IMP.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.IMP.get(),            ServerConfigs.IMP_WEIGHT.get(),              1, 3);
                if (CommonConfigs.ALLOW_SPAWN_FIRE_GHOST.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.FIRE_GHOST.get(),     ServerConfigs.FIRE_GHOST_NETHER_WEIGHT.get(), 1, 2);
                if (CommonConfigs.ALLOW_SPAWN_NETHER_SCORPION.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.NETHER_SCORPION.get(), ServerConfigs.NETHER_SCORPION_WEIGHT.get(),   1, 2);
            }

            if (biome.is(IS_NETHER)) {
                if (CommonConfigs.ALLOW_SPAWN_HELL_OGRE.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.HELL_OGRE.get(), ServerConfigs.HELL_OGRE_WEIGHT.get(), 1, 1);
                if (CommonConfigs.ALLOW_SPAWN_NIGHTMARE_HORSE.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.NIGHTMARE_HORSE.get(), ServerConfigs.NIGHTMARE_HORSE_WEIGHT.get(), 1, 1);
            }

            if (biome.is(SOUL_SAND_VALLEY)) {
                if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                    add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_NETHER_WEIGHT.get(), 1, 3);
            }

            addTwilightForestSpawns(biome, spawns);
            addCaveBiomesSpawns(biome, spawns);
        }
    }

    private static void add(MobSpawnSettingsBuilder builder, MobCategory category,
                             EntityType<?> type, int weight, int min, int max) {
        if (weight <= 0) return;
        builder.addSpawn(category, new MobSpawnSettings.SpawnerData(type, weight, min, max));
    }

    private static ResourceKey<Biome> tf(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("twilightforest", path));
    }

    private static final ResourceKey<Biome> TF_FOREST                = tf("forest");
    private static final ResourceKey<Biome> TF_DENSE_FOREST          = tf("dense_forest");
    private static final ResourceKey<Biome> TF_FIREFLY_FOREST        = tf("firefly_forest");
    private static final ResourceKey<Biome> TF_MUSHROOM_FOREST       = tf("mushroom_forest");
    private static final ResourceKey<Biome> TF_DENSE_MUSHROOM_FOREST = tf("dense_mushroom_forest");
    private static final ResourceKey<Biome> TF_CLEARING              = tf("clearing");
    private static final ResourceKey<Biome> TF_OAK_SAVANNAH          = tf("oak_savannah");
    private static final ResourceKey<Biome> TF_SPOOKY_FOREST         = tf("spooky_forest");
    private static final ResourceKey<Biome> TF_SWAMP                 = tf("swamp");
    private static final ResourceKey<Biome> TF_DARK_FOREST           = tf("dark_forest");
    private static final ResourceKey<Biome> TF_DARK_FOREST_CENTER    = tf("dark_forest_center");
    private static final ResourceKey<Biome> TF_SNOWY_FOREST          = tf("snowy_forest");
    private static final ResourceKey<Biome> TF_GLACIER               = tf("glacier");
    private static final ResourceKey<Biome> TF_HIGHLANDS             = tf("highlands");
    private static final ResourceKey<Biome> TF_THORNLANDS            = tf("thornlands");
    private static final ResourceKey<Biome> TF_FINAL_PLATEAU         = tf("final_plateau");
    private static final ResourceKey<Biome> TF_UNDERGROUND           = tf("underground");

    private static ResourceKey<Biome> ycb(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("yungscavebiomes", path));
    }

    private static final ResourceKey<Biome> YCB_FROSTED_CAVES = ycb("frosted_caves");
    private static final ResourceKey<Biome> YCB_LOST_CAVES    = ycb("lost_caves");

    private static void addCaveBiomesSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        if (biome.is(YCB_FROSTED_CAVES)) {
            if (CommonConfigs.ALLOW_SPAWN_ICE_GOLEM.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.ICE_GOLEM.get(),      ServerConfigs.ICE_GOLEM_WEIGHT.get(),      1, 1);
            if (CommonConfigs.ALLOW_SPAWN_FROST_SCORPION.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.FROST_SCORPION.get(), ServerConfigs.FROST_SCORPION_WEIGHT.get(), 1, 1);
        }

        if (biome.is(YCB_LOST_CAVES)) {
            if (CommonConfigs.ALLOW_SPAWN_DESERT_SCORPION.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.DESERT_SCORPION.get(), ServerConfigs.DESERT_SCORPION_WEIGHT.get(), 1, 1);
        }
    }

    @SafeVarargs
    private static boolean isAny(Holder<Biome> biome, ResourceKey<Biome>... keys) {
        for (ResourceKey<Biome> key : keys) if (biome.is(key)) return true;
        return false;
    }

    private static void addTwilightForestSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        if (biome.is(TF_DENSE_FOREST) && CommonConfigs.ALLOW_SPAWN_OWLBEAR.get())
            add(spawns, MobCategory.MONSTER, EntityRegistry.OWLBEAR.get(), ServerConfigs.OWLBEAR_WEIGHT.get(), 1, 1);

        if (biome.is(TF_CLEARING) && CommonConfigs.ALLOW_SPAWN_GOLEM.get())
            add(spawns, MobCategory.MONSTER, EntityRegistry.GOLEM.get(), ServerConfigs.GOLEM_WEIGHT.get(), 1, 1);

        if (isAny(biome, TF_FOREST, TF_DENSE_FOREST, TF_FIREFLY_FOREST, TF_MUSHROOM_FOREST,
                         TF_DENSE_MUSHROOM_FOREST, TF_CLEARING, TF_OAK_SAVANNAH)) {
            if (CommonConfigs.ALLOW_SPAWN_WILD_WOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WILD_WOLF.get(), ServerConfigs.WILD_WOLF_WEIGHT.get(), 1, 4);
            if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.EVIL_BAT.get(), ServerConfigs.EVIL_BAT_WEIGHT.get(), 1, 3);
        }

        if (isAny(biome, TF_OAK_SAVANNAH, TF_SWAMP) && CommonConfigs.ALLOW_SPAWN_SCORPION.get())
            add(spawns, MobCategory.MONSTER, EntityRegistry.SCORPION.get(), ServerConfigs.SCORPION_WEIGHT.get(), 1, 1);

        if (biome.is(TF_SWAMP)) {
            if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.EVIL_BAT.get(), ServerConfigs.EVIL_BAT_WEIGHT.get(), 1, 3);
            if (CommonConfigs.ALLOW_SPAWN_WILD_WOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WILD_WOLF.get(), ServerConfigs.WILD_WOLF_WEIGHT.get(), 1, 4);
        }

        if (biome.is(TF_SPOOKY_FOREST)) {
            if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_WEIGHT.get(), 1, 2);
            if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.EVIL_BAT.get(), ServerConfigs.EVIL_BAT_WEIGHT.get(), 1, 3);
            if (CommonConfigs.ALLOW_SPAWN_WEREWOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WEREWOLF.get(), ServerConfigs.WEREWOLF_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_HORSE_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.HORSE_BAT.get(), ServerConfigs.HORSE_BAT_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_NATURAL_SPAWN_SKELETON_HORSE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SKELETON_HORSE_MOB.get(), ServerConfigs.SKELETON_HORSE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.ZOMBIE_HORSE_MOB.get(), ServerConfigs.ZOMBIE_HORSE_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_DARK_FOREST)) {
            if (CommonConfigs.ALLOW_SPAWN_GOBLIN.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GOBLIN.get(), ServerConfigs.GOBLIN_WEIGHT.get(), 1, 4);
            if (CommonConfigs.ALLOW_SPAWN_DART_GOBLIN.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.DART_GOBLIN.get(), ServerConfigs.DART_GOBLIN_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_WEREWOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WEREWOLF.get(), ServerConfigs.WEREWOLF_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_WEIGHT.get(), 1, 2);
            if (CommonConfigs.ALLOW_SPAWN_HORSE_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.HORSE_BAT.get(), ServerConfigs.HORSE_BAT_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.EVIL_BAT.get(), ServerConfigs.EVIL_BAT_WEIGHT.get(), 1, 3);
        }

        if (biome.is(TF_DARK_FOREST_CENTER)) {
            if (CommonConfigs.ALLOW_SPAWN_GOBLIN.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GOBLIN.get(), ServerConfigs.GOBLIN_WEIGHT.get(), 2, 4);
            if (CommonConfigs.ALLOW_SPAWN_DART_GOBLIN.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.DART_GOBLIN.get(), ServerConfigs.DART_GOBLIN_WEIGHT.get(), 1, 2);
            if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.OGRE.get(), ServerConfigs.OGRE_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_SNOWY_FOREST)) {
            if (CommonConfigs.ALLOW_SPAWN_WILD_WOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WILD_WOLF.get(), ServerConfigs.WILD_WOLF_WEIGHT.get(), 1, 4);
            if (CommonConfigs.ALLOW_SPAWN_SHRIKE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SHRIKE.get(), ServerConfigs.SHRIKE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_FROST_SCORPION.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.FROST_SCORPION.get(), ServerConfigs.FROST_SCORPION_WEIGHT.get(), 1, 2);
            if (CommonConfigs.ALLOW_SPAWN_WEREWOLF.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.WEREWOLF.get(), ServerConfigs.WEREWOLF_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_GLACIER) && CommonConfigs.ALLOW_SPAWN_FROST_SCORPION.get())
            add(spawns, MobCategory.MONSTER, EntityRegistry.FROST_SCORPION.get(), ServerConfigs.FROST_SCORPION_WEIGHT.get(), 1, 2);

        if (biome.is(TF_HIGHLANDS)) {
            if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.OGRE.get(), ServerConfigs.OGRE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_CAVE_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.CAVE_OGRE.get(), ServerConfigs.CAVE_OGRE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_THORNLANDS)) {
            if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.OGRE.get(), ServerConfigs.OGRE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_CAVE_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.CAVE_OGRE.get(), ServerConfigs.CAVE_OGRE_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_FINAL_PLATEAU)) {
            if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_WEIGHT.get(), 1, 2);
            if (CommonConfigs.ALLOW_SPAWN_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.OGRE.get(), ServerConfigs.OGRE_WEIGHT.get(), 1, 1);
        }

        if (biome.is(TF_UNDERGROUND)) {
            if (CommonConfigs.ALLOW_SPAWN_CAVE_OGRE.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.CAVE_OGRE.get(), ServerConfigs.CAVE_OGRE_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_GOBLIN.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GOBLIN.get(), ServerConfigs.GOBLIN_WEIGHT.get(), 1, 4);
            if (CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.SILVER_SKELETON.get(), ServerConfigs.SILVER_SKELETON_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.EVIL_BAT.get(), ServerConfigs.EVIL_BAT_WEIGHT.get(), 1, 3);
            if (CommonConfigs.ALLOW_SPAWN_HORSE_BAT.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.HORSE_BAT.get(), ServerConfigs.HORSE_BAT_WEIGHT.get(), 1, 1);
            if (CommonConfigs.ALLOW_SPAWN_GHOST.get())
                add(spawns, MobCategory.MONSTER, EntityRegistry.GHOST.get(), ServerConfigs.GHOST_WEIGHT.get(), 1, 2);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
