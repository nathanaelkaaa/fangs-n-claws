package net.raptorzizi.fangs_n_claws.entity.shrike;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.owlbear.BabyOwlbearEntity;

public class BabyShrikeEntity extends BabyOwlbearEntity {

    public BabyShrikeEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public String textureBaseName() {
        return "baby_shrike";
    }
}
