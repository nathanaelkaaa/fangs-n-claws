package net.raptorzizi.fangs_n_claws.entity.cave_ogre;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;

public class CaveOgreEntity extends OgreEntity {

    public CaveOgreEntity(EntityType<? extends CaveOgreEntity> entityType, Level level) {
        super(entityType, level);
    }
}
