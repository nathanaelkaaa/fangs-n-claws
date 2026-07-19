package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class DesertScorpionEntity extends ScorpionEntity {

    private static final ResourceKey<Biome> YCB_LOST_CAVES =
            ResourceKey.create(Registries.BIOME, new ResourceLocation("yungscavebiomes", "lost_caves"));

    public DesertScorpionEntity(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Spider>) entityType, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (level instanceof ServerLevelAccessor sla && sla.getDifficulty() == Difficulty.PEACEFUL) return false;
        boolean lostCave = level.getBiome(this.blockPosition()).is(YCB_LOST_CAVES);
        if (!lostCave && (!(level instanceof Level worldLevel) || !worldLevel.isDay())) return false;
        return level.getBrightness(LightLayer.BLOCK, this.blockPosition()) <= 7;
    }

    @Override
    protected void applyHitEffect(LivingEntity living, boolean isStingerAttack) {
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true, true));
    }

    @Override
    public ResourceLocation textureLocation() {
        return FangsClawsMod.id("textures/entity/desert_scorpion.png");
    }

    @Override
    public EyeStyle eyeStyle() {
        return EyeStyle.NONE;
    }
}
