package net.raptorzizi.fangs_n_claws.entity.cave_ogre;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;

public class CaveOgreEntity extends OgreEntity {

    // Spawn

    public CaveOgreEntity(EntityType<? extends CaveOgreEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkCaveOgreSpawnRules(EntityType<? extends CaveOgreEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (pos.getY() >= 0) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}
