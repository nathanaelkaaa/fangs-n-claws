package net.raptorzizi.fangs_n_claws.entity.scorpion;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public class NetherScorpionEntity extends ScorpionEntity {

    public NetherScorpionEntity(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Spider>) entityType, level);
    }

    @Override
    protected void applyHitEffect(LivingEntity living, boolean isStingerAttack) {
        living.igniteForSeconds(5);
    }
}
