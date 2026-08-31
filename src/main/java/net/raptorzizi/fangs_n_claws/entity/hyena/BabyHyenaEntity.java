package net.raptorzizi.fangs_n_claws.entity.hyena;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.BabyWildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;

public class BabyHyenaEntity extends BabyWildWolfEntity {

    public static final TagKey<Item> HYENA_FOOD =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(
                    FangsClawsMod.MOD_ID, "hyena_food"));

    public BabyHyenaEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override public String textureBaseName() { return "baby_wild_hyena"; }
    @Override public String geoName()         { return "baby_wild_hyena"; }
    @Override public String animationName()   { return "baby_hyena"; }

    @Override
    public TagKey<Item> foodTag() { return HYENA_FOOD; }

    @Override
    protected Class<? extends WildWolfEntity> adultClass() { return HyenaEntity.class; }

    @Override
    protected EntityType<? extends WildWolfEntity> adultType() { return EntityRegistry.HYENA.get(); }
}
