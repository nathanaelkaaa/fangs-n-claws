package net.raptorzizi.fangs_n_claws.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue GOBLIN_WEIGHT;
    public static final ModConfigSpec.IntValue OGRE_WEIGHT;
    public static final ModConfigSpec.IntValue CAVE_OGRE_WEIGHT;
    public static final ModConfigSpec.IntValue GOLEM_WEIGHT;
    public static final ModConfigSpec.IntValue OWLBEAR_WEIGHT;
    public static final ModConfigSpec.IntValue SILVER_SKELETON_WEIGHT;
    public static final ModConfigSpec.IntValue EVIL_BAT_WEIGHT;
    public static final ModConfigSpec.IntValue GHOST_WEIGHT;
    public static final ModConfigSpec.IntValue GHOST_NETHER_WEIGHT;
    public static final ModConfigSpec.IntValue WEREWOLF_WEIGHT;

    static {
        BUILDER.comment("Fangs 'n Claws — Server Configuration").push("spawn_weights");
        BUILDER.comment("Spawn weight for each mob. Set to 0 to disable natural spawning.");

        GOBLIN_WEIGHT          = BUILDER.comment("Goblin (overworld, any biome) — default: 25")
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
        OWLBEAR_WEIGHT         = BUILDER.comment("Owlbear (forest only) — default: 8")
                                        .translation("fangs_n_claws.configuration.spawn_weights.owlbear")
                                        .defineInRange("owlbear",          8, 0, 500);
        SILVER_SKELETON_WEIGHT = BUILDER.comment("Silver Skeleton (overworld) — default: 30")
                                        .translation("fangs_n_claws.configuration.spawn_weights.silver_skeleton")
                                        .defineInRange("silver_skeleton", 30, 0, 500);
        EVIL_BAT_WEIGHT        = BUILDER.comment("Evil Bat (overworld) — default: 20")
                                        .translation("fangs_n_claws.configuration.spawn_weights.evil_bat")
                                        .defineInRange("evil_bat",        15, 0, 500);
        GHOST_WEIGHT           = BUILDER.comment("Ghost (overworld) — default: 25")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ghost")
                                        .defineInRange("ghost",           25, 0, 500);
        GHOST_NETHER_WEIGHT    = BUILDER.comment("Ghost (Soul Sand Valley) — default: 15")
                                        .translation("fangs_n_claws.configuration.spawn_weights.ghost_nether")
                                        .defineInRange("ghost_nether",    15, 0, 500);
        WEREWOLF_WEIGHT        = BUILDER.comment("Werewolf (overworld) — default: 30")
                                        .translation("fangs_n_claws.configuration.spawn_weights.werewolf")
                                        .defineInRange("werewolf",        25, 0, 500);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}