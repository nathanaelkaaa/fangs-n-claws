package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class NetherScorpionEntity extends ScorpionEntity {

    public NetherScorpionEntity(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Spider>) entityType, level);
    }

    @Override
    protected void applyHitEffect(LivingEntity living, boolean isStingerAttack) {
        living.igniteForSeconds(5);
    }

    @Override
    public ResourceLocation textureLocation() {
        return FangsClawsMod.id("textures/entity/nether_scorpion.png");
    }

    @Override
    public ResourceLocation eyesTexture() {
        return FangsClawsMod.id("textures/entity/glowing_eyes/nether_scorpion_eyes.png");
    }

    @Override
    public EyeStyle eyeStyle() {
        return EyeStyle.EMISSIVE;
    }
}
