package net.raptorzizi.fangs_n_claws.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommonConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final Map<ResourceLocation, ModConfigSpec.IntValue> DIMENSION_CAPS = new LinkedHashMap<>();

    // Spawn toggles
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GOBLIN;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_CAVE_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GOLEM;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_OWLBEAR;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_SHRIKE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_SILVER_SKELETON;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_EVIL_BAT;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GHOST;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_WEREWOLF;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_DART_GOBLIN;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_IMP;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_HELL_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_SCORPION;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_ICE_GOLEM;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_FIRE_GHOST;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_DESERT_SCORPION;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_FROST_SCORPION;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_NETHER_SCORPION;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_HORSE_BAT;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_NIGHTMARE_HORSE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_WILD_WOLF;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_MIMIC;
    public static final ModConfigSpec.BooleanValue ALLOW_NATURAL_SPAWN_SKELETON_HORSE;
    public static final ModConfigSpec.BooleanValue ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE;

    public static final ModConfigSpec.BooleanValue VANILLA_SKELETON_HORSE;
    public static final ModConfigSpec.BooleanValue VANILLA_ZOMBIE_HORSE;
    public static final ModConfigSpec.BooleanValue ALLOW_GOBLIN_STEALING;

    public static final ModConfigSpec.IntValue MIMIC_SPAWN_CHANCE;

    static {
        BUILDER.comment("Fangs 'n Claws — Common Configuration").push("spawn_toggles");
        BUILDER.comment("Enable or disable natural spawning for each mob. Can also be controlled per-world via /gamerule.");

        ALLOW_SPAWN_GOBLIN = BUILDER
                .comment("Allow Goblins to spawn naturally (overworld, night only)")
                .translation("fangs_n_claws.configuration.spawn_toggles.goblin")
                .define("allow_goblin", true);

        ALLOW_SPAWN_OGRE = BUILDER
                .comment("Allow Ogres to spawn naturally (overworld)")
                .translation("fangs_n_claws.configuration.spawn_toggles.ogre")
                .define("allow_ogre", true);

        ALLOW_SPAWN_CAVE_OGRE = BUILDER
                .comment("Allow Cave Ogres to spawn naturally (caves)")
                .translation("fangs_n_claws.configuration.spawn_toggles.cave_ogre")
                .define("allow_cave_ogre", true);

        ALLOW_SPAWN_GOLEM = BUILDER
                .comment("Allow Big Golems to spawn naturally (plains)")
                .translation("fangs_n_claws.configuration.spawn_toggles.golem")
                .define("allow_golem", true);

        ALLOW_SPAWN_OWLBEAR = BUILDER
                .comment("Allow Owlbears to spawn naturally (forest + taiga)")
                .translation("fangs_n_claws.configuration.spawn_toggles.owlbear")
                .define("allow_owlbear", true);

        ALLOW_SPAWN_SHRIKE = BUILDER
                .comment("Allow Shrikes to spawn naturally (snowy forest + snowy taiga)")
                .translation("fangs_n_claws.configuration.spawn_toggles.shrike")
                .define("allow_shrike", true);

        ALLOW_SPAWN_SILVER_SKELETON = BUILDER
                .comment("Allow Silver Skeletons to spawn naturally (overworld)")
                .translation("fangs_n_claws.configuration.spawn_toggles.silver_skeleton")
                .define("allow_silver_skeleton", true);

        ALLOW_SPAWN_EVIL_BAT = BUILDER
                .comment("Allow Evil Bats to spawn naturally (overworld)")
                .translation("fangs_n_claws.configuration.spawn_toggles.evil_bat")
                .define("allow_evil_bat", true);

        ALLOW_SPAWN_GHOST = BUILDER
                .comment("Allow Ghosts to spawn naturally (overworld + Soul Sand Valley)")
                .translation("fangs_n_claws.configuration.spawn_toggles.ghost")
                .define("allow_ghost", true);

        ALLOW_SPAWN_WEREWOLF = BUILDER
                .comment("Allow Werewolves to spawn naturally (overworld)")
                .translation("fangs_n_claws.configuration.spawn_toggles.werewolf")
                .define("allow_werewolf", true);

        ALLOW_SPAWN_DART_GOBLIN = BUILDER
                .comment("Allow Dart Goblins to spawn naturally (overworld, night only)")
                .translation("fangs_n_claws.configuration.spawn_toggles.dart_goblin")
                .define("allow_dart_goblin", true);

        ALLOW_SPAWN_IMP = BUILDER
                .comment("Allow Imps to spawn naturally (nether, any biome)")
                .translation("fangs_n_claws.configuration.spawn_toggles.imp")
                .define("allow_imp", true);

        ALLOW_SPAWN_HELL_OGRE = BUILDER
                .comment("Allow Hell Ogres to spawn naturally (nether)")
                .translation("fangs_n_claws.configuration.spawn_toggles.hell_ogre")
                .define("allow_hell_ogre", true);

        ALLOW_SPAWN_SCORPION = BUILDER
                .comment("Allow Scorpions to spawn naturally (overworld)")
                .translation("fangs_n_claws.configuration.spawn_toggles.scorpion")
                .define("allow_scorpion", true);

        ALLOW_SPAWN_ICE_GOLEM = BUILDER
                .comment("Allow Ice Golems to spawn naturally (snowy biomes)")
                .translation("fangs_n_claws.configuration.spawn_toggles.ice_golem")
                .define("allow_ice_golem", true);

        ALLOW_SPAWN_FIRE_GHOST = BUILDER
                .comment("Allow Fire Ghosts to spawn naturally (overworld + Crimson Forest + Nether Wastes)")
                .translation("fangs_n_claws.configuration.spawn_toggles.fire_ghost")
                .define("allow_fire_ghost", true);

        ALLOW_SPAWN_DESERT_SCORPION = BUILDER
                .comment("Allow Desert Scorpions to spawn naturally (desert, daytime)")
                .translation("fangs_n_claws.configuration.spawn_toggles.desert_scorpion")
                .define("allow_desert_scorpion", true);

        ALLOW_SPAWN_FROST_SCORPION = BUILDER
                .comment("Allow Frost Scorpions to spawn naturally (snowy biomes)")
                .translation("fangs_n_claws.configuration.spawn_toggles.frost_scorpion")
                .define("allow_frost_scorpion", true);

        ALLOW_SPAWN_NETHER_SCORPION = BUILDER
                .comment("Allow Nether Scorpions to spawn naturally (Crimson Forest + Nether Wastes)")
                .translation("fangs_n_claws.configuration.spawn_toggles.nether_scorpion")
                .define("allow_nether_scorpion", true);

        ALLOW_SPAWN_HORSE_BAT = BUILDER
                .comment("Allow Horse Bats to spawn naturally")
                .translation("fangs_n_claws.configuration.spawn_toggles.horse_bat")
                .define("allow_horse_bat", true);

        ALLOW_SPAWN_NIGHTMARE_HORSE = BUILDER
                .comment("Allow Nightmare Horses to spawn naturally (Nether)")
                .translation("fangs_n_claws.configuration.spawn_toggles.nightmare_horse")
                .define("allow_nightmare_horse", true);

        ALLOW_SPAWN_WILD_WOLF = BUILDER
                .comment("Allow Wild Wolves to spawn naturally")
                .translation("fangs_n_claws.configuration.spawn_toggles.wild_wolf")
                .define("allow_wild_wolf", true);

        ALLOW_SPAWN_MIMIC = BUILDER
                .comment("Allow Mimics to replace loot chests in structures (also /gamerule allowSpawnMimic). See mimic_spawn_chance for the rate.")
                .translation("fangs_n_claws.configuration.spawn_toggles.mimic")
                .define("allow_mimic", true);

        BUILDER.pop();

        BUILDER.comment("Mob behaviour toggles. Each mirrors a /gamerule of the same name — changing either applies.").push("behavior");

        VANILLA_SKELETON_HORSE = BUILDER
                .comment("Keep the vanilla Skeleton Horse instead of the mod's tamable version (also /gamerule vanillaSkeletonHorse)")
                .translation("fangs_n_claws.configuration.behavior.vanilla_skeleton_horse")
                .define("vanilla_skeleton_horse", false);

        VANILLA_ZOMBIE_HORSE = BUILDER
                .comment("Keep the vanilla Zombie Horse instead of the mod's tamable version (also /gamerule vanillaZombieHorse)")
                .translation("fangs_n_claws.configuration.behavior.vanilla_zombie_horse")
                .define("vanilla_zombie_horse", false);

        ALLOW_NATURAL_SPAWN_SKELETON_HORSE = BUILDER
                .comment("Allow Skeleton Horses to spawn naturally at night, whichever version is used (also /gamerule allowNaturalSpawnSkeletonHorse)")
                .translation("fangs_n_claws.configuration.behavior.allow_natural_spawn_skeleton_horse")
                .define("allow_natural_spawn_skeleton_horse", true);

        ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE = BUILDER
                .comment("Allow Zombie Horses to spawn naturally at night, whichever version is used (also /gamerule allowNaturalSpawnZombieHorse)")
                .translation("fangs_n_claws.configuration.behavior.allow_natural_spawn_zombie_horse")
                .define("allow_natural_spawn_zombie_horse", true);

        ALLOW_GOBLIN_STEALING = BUILDER
                .comment("Allow Goblins to steal items from players' inventories (also /gamerule allowGoblinStealing)")
                .translation("fangs_n_claws.configuration.behavior.allow_goblin_stealing")
                .define("allow_goblin_stealing", true);

        MIMIC_SPAWN_CHANCE = BUILDER
                .comment("When a loot chest is generated in a structure (stronghold, mineshaft, temple, dungeon, ...),",
                         "it has a 1/x chance of being replaced by a Mimic that steals the chest's loot.",
                         "Higher value = fewer mimics. 1 = every structure chest is a mimic.",
                         "Set very high (e.g. 100000) to effectively disable structure mimics.")
                .translation("fangs_n_claws.configuration.behavior.mimic_spawn_chance")
                .defineInRange("mimic_spawn_chance", 20, 1, 100000);

        BUILDER.pop();

        BUILDER.comment("Cap Fangs 'n Claws mob density per dimension.",
                        "For each: -1 = unlimited, 0 = no F&C spawns, N = density cap (N scaled by loaded chunks / 289, like the vanilla mob cap).")
                .translation("fangs_n_claws.configuration.spawn_limits")
                .push("spawn_limits");

        defineDimensionCap("minecraft:overworld",   "overworld",   40);
        defineDimensionCap("minecraft:the_nether",  "the_nether",  20);

        if (ModList.get().isLoaded("twilightforest")) {
            defineDimensionCap("twilightforest:twilight_forest", "twilight_forest", 15);
        }

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private static void defineDimensionCap(String dimensionId, String key, int defaultCap) {
        ModConfigSpec.IntValue value = BUILDER
                .translation("fangs_n_claws.configuration.spawn_limits." + key)
                .defineInRange(key, defaultCap, -1, 100000);
        DIMENSION_CAPS.put(ResourceLocation.parse(dimensionId), value);
    }
}
