package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.world.level.GameRules;

public class GameRuleRegistry {

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GOBLIN =
            GameRules.register("allowSpawnGoblin",          GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_OGRE =
            GameRules.register("allowSpawnOgre",            GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_CAVE_OGRE =
            GameRules.register("allowSpawnCaveOgre",        GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_HELL_OGRE =
            GameRules.register("allowSpawnHellOgre",        GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GOLEM =
            GameRules.register("allowSpawnGolem",           GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_OWLBEAR =
            GameRules.register("allowSpawnOwlbear",         GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SILVER_SKELETON =
            GameRules.register("allowSpawnSilverSkeleton",  GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_EVIL_BAT =
            GameRules.register("allowSpawnEvilBat",         GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GHOST =
            GameRules.register("allowSpawnGhost",           GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_WEREWOLF =
            GameRules.register("allowSpawnWerewolf",        GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_DART_GOBLIN =
            GameRules.register("allowSpawnDartGoblin",      GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_IMP =
            GameRules.register("allowSpawnImp",             GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SCORPION =
            GameRules.register("allowSpawnScorpion",        GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static void init() {}
}
