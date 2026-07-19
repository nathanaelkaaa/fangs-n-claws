package net.raptorzizi.fangs_n_claws.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GOBLIN_WEIGHT;
    public static final ForgeConfigSpec.IntValue OGRE_WEIGHT;
    public static final ForgeConfigSpec.IntValue CAVE_OGRE_WEIGHT;
    public static final ForgeConfigSpec.IntValue GOLEM_WEIGHT;
    public static final ForgeConfigSpec.IntValue OWLBEAR_WEIGHT;
    public static final ForgeConfigSpec.IntValue SHRIKE_WEIGHT;
    public static final ForgeConfigSpec.IntValue SILVER_SKELETON_WEIGHT;
    public static final ForgeConfigSpec.IntValue EVIL_BAT_WEIGHT;
    public static final ForgeConfigSpec.IntValue GHOST_WEIGHT;
    public static final ForgeConfigSpec.IntValue GHOST_NETHER_WEIGHT;
    public static final ForgeConfigSpec.IntValue WEREWOLF_WEIGHT;
    public static final ForgeConfigSpec.IntValue DART_GOBLIN_WEIGHT;
    public static final ForgeConfigSpec.IntValue IMP_WEIGHT;
    public static final ForgeConfigSpec.IntValue HELL_OGRE_WEIGHT;
    public static final ForgeConfigSpec.IntValue SCORPION_WEIGHT;
    public static final ForgeConfigSpec.IntValue ICE_GOLEM_WEIGHT;
    public static final ForgeConfigSpec.IntValue FIRE_GHOST_WEIGHT;
    public static final ForgeConfigSpec.IntValue FIRE_GHOST_NETHER_WEIGHT;
    public static final ForgeConfigSpec.IntValue DESERT_SCORPION_WEIGHT;
    public static final ForgeConfigSpec.IntValue FROST_SCORPION_WEIGHT;
    public static final ForgeConfigSpec.IntValue NETHER_SCORPION_WEIGHT;
    public static final ForgeConfigSpec.IntValue HORSE_BAT_WEIGHT;
    public static final ForgeConfigSpec.IntValue WILD_WOLF_WEIGHT;
    public static final ForgeConfigSpec.IntValue NIGHTMARE_HORSE_WEIGHT;
    public static final ForgeConfigSpec.IntValue SKELETON_HORSE_WEIGHT;
    public static final ForgeConfigSpec.IntValue ZOMBIE_HORSE_WEIGHT;

    static {
        BUILDER.comment("Fangs 'n Claws — Server Configuration").push("spawn_weights");
        BUILDER.comment("Spawn weight for each mob. Set to 0 to disable natural spawning.");

        GOBLIN_WEIGHT          = BUILDER.comment("Goblin (overworld, any biome) — default: 20")
                                        .translation("fangs_n_claws.configuration.spawn_weights.goblin")
                                        .defineInRange("goblin",          20, 0, 500);
        OGRE_WEIGHT            = BUILDER.comment("Ogre (overworld, any biome) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ogre")
                                        .defineInRange("ogre",            15, 0, 500);
        CAVE_OGRE_WEIGHT       = BUILDER.comment("Cave Ogre (caves only) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.cave_ogre")
                                        .defineInRange("cave_ogre",       15, 0, 500);
        GOLEM_WEIGHT           = BUILDER.comment("Big Golem (plains only) — default: 5")
                                        .translation("fangs_n_claws.configuration.spawn_weights.golem")
                                        .defineInRange("golem",            5, 0, 500);
        OWLBEAR_WEIGHT         = BUILDER.comment("Owlbear (forest + taiga) — default: 8")
                                        .translation("fangs_n_claws.configuration.spawn_weights.owlbear")
                                        .defineInRange("owlbear",          8, 0, 500);
        SHRIKE_WEIGHT          = BUILDER.comment("Shrike (snowy forest + snowy taiga) — default: 5")
                                        .translation("fangs_n_claws.configuration.spawn_weights.shrike")
                                        .defineInRange("shrike",           5, 0, 500);
        SILVER_SKELETON_WEIGHT = BUILDER.comment("Silver Skeleton (overworld) — default: 20")
                                        .translation("fangs_n_claws.configuration.spawn_weights.silver_skeleton")
                                        .defineInRange("silver_skeleton", 20, 0, 500);
        EVIL_BAT_WEIGHT        = BUILDER.comment("Evil Bat (overworld) — default: 20")
                                        .translation("fangs_n_claws.configuration.spawn_weights.evil_bat")
                                        .defineInRange("evil_bat",        20, 0, 500);
        GHOST_WEIGHT           = BUILDER.comment("Ghost (overworld) — default: 25")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ghost")
                                        .defineInRange("ghost",           25, 0, 500);
        GHOST_NETHER_WEIGHT    = BUILDER.comment("Ghost (Soul Sand Valley) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ghost_nether")
                                        .defineInRange("ghost_nether",    15, 0, 500);
        WEREWOLF_WEIGHT        = BUILDER.comment("Werewolf (overworld) — default: 25")
                                        .translation("fangs_n_claws.configuration.spawn_weights.werewolf")
                                        .defineInRange("werewolf",        25, 0, 500);

        DART_GOBLIN_WEIGHT     = BUILDER.comment("Dart Goblin (overworld, any biome) — default: 5")
                                        .translation("fangs_n_claws.configuration.spawn_weights.dart_goblin")
                                        .defineInRange("dart_goblin",      5, 0, 500);
        IMP_WEIGHT             = BUILDER.comment("Imp (Crimson Forest + Nether Wastes) — default: 10")
                                        .translation("fangs_n_claws.configuration.spawn_weights.imp")
                                        .defineInRange("imp",             10, 0, 500);
        HELL_OGRE_WEIGHT       = BUILDER.comment("Hell Ogre (nether, all biomes) — default: 10")
                                        .translation("fangs_n_claws.configuration.spawn_weights.hell_ogre")
                                        .defineInRange("hell_ogre",       10, 0, 500);
        SCORPION_WEIGHT        = BUILDER.comment("Scorpion (overworld, any biome) — default: 25")
                                        .translation("fangs_n_claws.configuration.spawn_weights.scorpion")
                                        .defineInRange("scorpion",        25, 0, 500);
        ICE_GOLEM_WEIGHT       = BUILDER.comment("Ice Golem (snowy biomes) — default: 5")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ice_golem")
                                        .defineInRange("ice_golem",        5, 0, 500);
        FIRE_GHOST_WEIGHT      = BUILDER.comment("Fire Ghost (overworld, rare) — default: 10")
                                        .translation("fangs_n_claws.configuration.spawn_weights.fire_ghost")
                                        .defineInRange("fire_ghost",       10, 0, 500);
        FIRE_GHOST_NETHER_WEIGHT = BUILDER.comment("Fire Ghost (Crimson Forest + Nether Wastes) — default: 12")
                                        .translation("fangs_n_claws.configuration.spawn_weights.fire_ghost_nether")
                                        .defineInRange("fire_ghost_nether", 12, 0, 500);
        DESERT_SCORPION_WEIGHT   = BUILDER.comment("Desert Scorpion (desert, daytime) — default: 20")
                                        .translation("fangs_n_claws.configuration.spawn_weights.desert_scorpion")
                                        .defineInRange("desert_scorpion",   20, 0, 500);
        FROST_SCORPION_WEIGHT    = BUILDER.comment("Frost Scorpion (snowy biomes) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.frost_scorpion")
                                        .defineInRange("frost_scorpion",    15, 0, 500);
        NETHER_SCORPION_WEIGHT   = BUILDER.comment("Nether Scorpion (Crimson Forest + Nether Wastes) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.nether_scorpion")
                                        .defineInRange("nether_scorpion",   15, 0, 500);
        HORSE_BAT_WEIGHT         = BUILDER.comment("Horse Bat (overworld) — default: 3")
                                        .translation("fangs_n_claws.configuration.spawn_weights.horse_bat")
                                        .defineInRange("horse_bat",          3, 0, 500);
        WILD_WOLF_WEIGHT         = BUILDER.comment("Wild Wolf (overworld) — default: 8 (vanilla wolf weight)")
                                        .translation("fangs_n_claws.configuration.spawn_weights.wild_wolf")
                                        .defineInRange("wild_wolf",          8, 0, 500);
        NIGHTMARE_HORSE_WEIGHT   = BUILDER.comment("Nightmare Horse (Nether) — default: 8")
                                        .translation("fangs_n_claws.configuration.spawn_weights.nightmare_horse")
                                        .defineInRange("nightmare_horse",    8, 0, 500);
        SKELETON_HORSE_WEIGHT    = BUILDER.comment("Skeleton Horse (overworld, night) — default: 3")
                                        .translation("fangs_n_claws.configuration.spawn_weights.skeleton_horse")
                                        .defineInRange("skeleton_horse",     3, 0, 500);
        ZOMBIE_HORSE_WEIGHT      = BUILDER.comment("Zombie Horse (overworld, night) — default: 3")
                                        .translation("fangs_n_claws.configuration.spawn_weights.zombie_horse")
                                        .defineInRange("zombie_horse",       3, 0, 500);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}