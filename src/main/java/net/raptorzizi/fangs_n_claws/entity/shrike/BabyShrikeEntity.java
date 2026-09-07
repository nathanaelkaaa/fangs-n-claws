package net.raptorzizi.fangs_n_claws.entity.shrike;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.owlbear.BabyOwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class BabyShrikeEntity extends BabyOwlbearEntity {

    public BabyShrikeEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public String textureBaseName() {
        return "baby_shrike";
    }

    @Override
    protected EntityType<? extends OwlbearEntity> adultType() {
        return EntityRegistry.SHRIKE.get();
    }
}
