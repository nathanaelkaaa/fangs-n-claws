package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

public class FrostScorpionEntity extends ScorpionEntity {

    public FrostScorpionEntity(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Spider>) entityType, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (level instanceof ServerLevelAccessor sla && sla.getDifficulty() == Difficulty.PEACEFUL) return false;
        return level.getBrightness(LightLayer.BLOCK, this.blockPosition()) <= 7;
    }

    @Override
    protected void applyHitEffect(LivingEntity living, boolean isStingerAttack) {
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true, true));
    }
}
