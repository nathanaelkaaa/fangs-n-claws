package net.raptorzizi.fangs_n_claws.entity.ent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.OptionalInt;

/**
 * L'ent de bouleau. Il garde le comportement de l'ent de chene ; son type d'entite lui donne sa propre
 * loot table, entities/birch_ent, et ses feuilles gardent la teinte fixe des feuilles de bouleau.
 */
public class BirchEntEntity extends EntEntity {

    private static final ResourceLocation TEXTURE = FangsClawsMod.id("textures/entity/ent_birch.png");

    public BirchEntEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public ResourceLocation textureLocation() {
        return TEXTURE;
    }

    @Override
    public OptionalInt fixedFoliageColor() {
        return OptionalInt.of(FoliageColor.getBirchColor());
    }
}
