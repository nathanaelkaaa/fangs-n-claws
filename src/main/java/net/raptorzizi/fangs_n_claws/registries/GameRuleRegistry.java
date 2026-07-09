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

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SHRIKE =
            GameRules.register("allowSpawnShrike",          GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SILVER_SKELETON =
            GameRules.register("allowSpawnSilverSkeleton",  GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_EVIL_BAT =
            GameRules.register("allowSpawnEvilBat",         GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_GHOST =
            GameRules.register("allowSpawnGhost",           GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_WEREWOLF =
            GameRules.register("allowSpawnWerewolf",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_DART_GOBLIN =
            GameRules.register("allowSpawnDartGoblin",      GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_IMP =
            GameRules.register("allowSpawnImp",             GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_HELL_OGRE =
            GameRules.register("allowSpawnHellOgre",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_SCORPION =
            GameRules.register("allowSpawnScorpion",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_ICE_GOLEM =
            GameRules.register("allowSpawnIceGolem",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_FIRE_GHOST =
            GameRules.register("allowSpawnFireGhost",       GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_DESERT_SCORPION =
            GameRules.register("allowSpawnDesertScorpion",  GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_FROST_SCORPION =
            GameRules.register("allowSpawnFrostScorpion",   GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_NETHER_SCORPION =
            GameRules.register("allowSpawnNetherScorpion",  GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_HORSE_BAT =
            GameRules.register("allowSpawnHorseBat",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_WILD_WOLF =
            GameRules.register("allowSpawnWildWolf",        GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_NIGHTMARE_HORSE =
            GameRules.register("allowSpawnNightmareHorse",  GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_NATURAL_SPAWN_SKELETON_HORSE =
            GameRules.register("allowNaturalSpawnSkeletonHorse", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE =
            GameRules.register("allowNaturalSpawnZombieHorse",   GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> VANILLA_SKELETON_HORSE =
            GameRules.register("vanillaSkeletonHorse",      GameRules.Category.MOBS, GameRules.BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> VANILLA_ZOMBIE_HORSE =
            GameRules.register("vanillaZombieHorse",        GameRules.Category.MOBS, GameRules.BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_GOBLIN_STEALING =
            GameRules.register("allowGoblinStealing",       GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static void init() {}
}
