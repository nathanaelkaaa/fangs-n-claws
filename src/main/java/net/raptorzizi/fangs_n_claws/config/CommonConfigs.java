package net.raptorzizi.fangs_n_claws.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Spawn toggles
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GOBLIN;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_CAVE_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GOLEM;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_OWLBEAR;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_SILVER_SKELETON;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_EVIL_BAT;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_GHOST;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_WEREWOLF;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_DART_GOBLIN;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_IMP;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_HELL_OGRE;
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_SCORPION;

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
                .comment("Allow Owlbears to spawn naturally (forest)")
                .translation("fangs_n_claws.configuration.spawn_toggles.owlbear")
                .define("allow_owlbear", true);

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

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
