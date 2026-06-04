package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.world.level.GameRules;

public class GameRuleRegistry {

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GOBLIN =
            GameRules.register("allowSpawnGoblin",          GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_OGRE =
            GameRules.register("allowSpawnOgre",            GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_CAVE_OGRE =
            GameRules.register("allowSpawnCaveOgre",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GOLEM =
            GameRules.register("allowSpawnGolem",           GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_OWLBEAR =
            GameRules.register("allowSpawnOwlbear",         GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SILVER_SKELETON =
            GameRules.register("allowSpawnSilverSkeleton",  GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_EVIL_BAT =
            GameRules.register("allowSpawnEvilBat",         GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GHOST =
            GameRules.register("allowSpawnGhost",           GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_WEREWOLF =
            GameRules.register("allowSpawnWerewolf",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static void init() {}
}
