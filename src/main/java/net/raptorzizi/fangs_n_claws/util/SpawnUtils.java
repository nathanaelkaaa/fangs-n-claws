package net.raptorzizi.fangs_n_claws.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.horse_bat.HorseBatEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;

public class SpawnUtils {

    public static boolean checkOgreSpawnRules(EntityType<? extends OgreEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (pos.getY() < 0) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkCaveOgreSpawnRules(EntityType<? extends CaveOgreEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (pos.getY() >= 0) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkWerewolfSpawnRules(EntityType<? extends WerewolfEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            AABB chunkBounds = new AABB(
                    chunkX << 4, level.getMinBuildHeight(), chunkZ << 4,
                    (chunkX << 4) + 16, level.getMaxBuildHeight(), (chunkZ << 4) + 16);
            if (serverLevel.getEntitiesOfClass(WerewolfEntity.class, chunkBounds).size() >= 2) return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkEvilBatSpawnRules(EntityType<? extends EvilBatEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) return false;
        int brightness = level.getLevel().isThundering()
                ? level.getMaxLocalRawBrightness(pos, 10)
                : level.getMaxLocalRawBrightness(pos);
        if (brightness > random.nextInt(8)) return false;
        /*if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.isDay()) return false;
            int maxGroup = switch (level.getDifficulty()) {
                case EASY   -> 3;
                case NORMAL -> 4;
                default     -> 5;
            };
            long nearby = serverLevel.getEntitiesOfClass(EvilBatEntity.class, new AABB(pos).inflate(24, 8, 24)).size();
            if (nearby >= maxGroup) return false;
        }*/
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkGhostSpawnRules(EntityType<? extends GhostEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) return false;
        int brightness = level.getLevel().isThundering()
                ? level.getMaxLocalRawBrightness(pos, 10)
                : level.getMaxLocalRawBrightness(pos);
        if (brightness > random.nextInt(8)) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkGolemSpawnRules(EntityType<? extends GolemEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        if (level instanceof ServerLevel serverLevel) {
            AABB area = new AABB(pos).inflate(48, 16, 48);
            if (!serverLevel.getEntitiesOfClass(GolemEntity.class, area).isEmpty()) return false;
        }
        return true;
    }

    public static boolean checkOwlbearSpawnRules(EntityType<? extends OwlbearEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        if (level instanceof ServerLevel serverLevel) {
            AABB area = new AABB(pos).inflate(48, 16, 48);
            if (!serverLevel.getEntitiesOfClass(OwlbearEntity.class, area).isEmpty()) return false;
        }
        return true;
    }

    public static boolean checkGoblinSpawnRules(EntityType<? extends GoblinEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        /*if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.isDay()) return false;
            int maxGroup = switch (level.getDifficulty()) {
                case EASY   -> 2;
                case NORMAL -> 3;
                default     -> 4;
            };
            long nearby = serverLevel.getEntitiesOfClass(GoblinEntity.class, new AABB(pos).inflate(24, 8, 24)).size();
            if (nearby >= maxGroup) return false;
        }*/
        return Monster.checkMonsterSpawnRules((EntityType<? extends Monster>)(EntityType<?>) type, level, spawnType, pos, random);
    }

    public static boolean checkDartGoblinSpawnRules(EntityType<? extends DartGoblinEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.isDay()) return false;
            int maxGroup = switch (level.getDifficulty()) {
                case EASY   -> 2;
                case NORMAL -> 3;
                default     -> 4;
            };
            long nearby = serverLevel.getEntitiesOfClass(DartGoblinEntity.class,
                    new AABB(pos).inflate(24, 8, 24)).size();
            if (nearby >= maxGroup) return false;
        }
        return Monster.checkMonsterSpawnRules((EntityType<? extends Monster>)(EntityType<?>) type, level, spawnType, pos, random);
    }

    public static boolean checkImpSpawnRules(EntityType<? extends ImpEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            int maxGroup = switch (level.getDifficulty()) {
                case EASY   -> 2;
                case NORMAL -> 3;
                default     -> 4;
            };
            long nearby = serverLevel.getEntitiesOfClass(
                    ImpEntity.class, new AABB(pos).inflate(24, 8, 24)).size();
            if (nearby >= maxGroup) return false;
        }
        return Monster.checkMonsterSpawnRules((EntityType<? extends Monster>)(EntityType<?>) type, level, spawnType, pos, random);
    }

    public static boolean checkHellOgreSpawnRules(EntityType<? extends HellOgreEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkScorpionSpawnRules(EntityType<? extends ScorpionEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkIceGolemSpawnRules(EntityType<? extends IceGolemEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        if (level instanceof ServerLevel serverLevel) {
            AABB area = new AABB(pos).inflate(48, 16, 48);
            if (!serverLevel.getEntitiesOfClass(IceGolemEntity.class, area).isEmpty()) return false;
        }
        return true;
    }

    public static boolean checkSilverSkeletonSpawnRules(EntityType<? extends SilverSkeletonEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean checkHorseBatSpawnRules(EntityType<? extends HorseBatEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules((EntityType<? extends Monster>) (EntityType<?>) type,
                level, spawnType, pos, random);
    }

    /*public static boolean checkWerevillagerSpawnRules(EntityType<WerevillagerEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level instanceof ServerLevel serverLevel && !serverLevel.isDay()) return false;
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }*/
}
