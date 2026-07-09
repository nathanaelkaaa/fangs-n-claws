package net.raptorzizi.fangs_n_claws.entity.fire_ghost;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import org.jetbrains.annotations.NotNull;

public class FireGhostEntity extends GhostEntity {

    public FireGhostEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            target.setSecondsOnFire(5);
        }
        return hit;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,                8.0)
                .add(Attributes.MOVEMENT_SPEED,             0.0)
                .add(Attributes.ATTACK_DAMAGE,              3.0)
                .add(Attributes.FOLLOW_RANGE,              20.0)
                .add(Attributes.FLYING_SPEED,               0.14);
    }
}
