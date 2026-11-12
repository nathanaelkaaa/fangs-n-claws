package net.raptorzizi.fangs_n_claws.item.amulet;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public enum AmuletCategory {

    UNDEAD("undead"),
    CHITIN("chitin"),
    FIN("fin"),
    WING("wing"),
    HOOF("hoof"),
    FANG("fang");

    // Fallback thresholds
    private static final float LARGE_WIDTH = 1.5F;

    public static final TagKey<EntityType<?>> UNAMULETABLE =
            TagKey.create(Registries.ENTITY_TYPE, FangsClawsMod.id("familiar/unamuletable"));

    private final String id;
    private final TagKey<EntityType<?>> tag;

    AmuletCategory(String id) {
        this.id = id;
        this.tag = TagKey.create(Registries.ENTITY_TYPE, FangsClawsMod.id("familiar/" + id));
    }

    public String id() {
        return this.id;
    }

    public TagKey<EntityType<?>> tag() {
        return this.tag;
    }

    public static boolean isFamiliarOf(Player player, Entity target) {
        if (target instanceof Player) return false;
        if (target.getType().is(UNAMULETABLE)) return false;

        return target instanceof OwnableEntity ownable
                && player.getUUID().equals(ownable.getOwnerUUID());
    }

    public boolean accepts(LivingEntity target) {
        if (target.getType().is(this.tag)) return true;

        for (AmuletCategory other : values()) {
            if (target.getType().is(other.tag)) return false;
        }

        return classify(target) == this;
    }

    public static AmuletCategory classify(LivingEntity target) {
        if (target.getType().is(EntityTypeTags.UNDEAD) || target.isInvertedHealAndHarm()) {
            return UNDEAD;
        }
        if (target.getType().is(EntityTypeTags.ARTHROPOD)) {
            return CHITIN;
        }
        if (isAquatic(target)) {
            return FIN;
        }
        if (isAerial(target)) {
            return WING;
        }
        if (target instanceof PlayerRideable || target.getBbWidth() > LARGE_WIDTH) {
            return HOOF;
        }
        return FANG;
    }

    private static boolean isAquatic(LivingEntity target) {
        MobCategory category = target.getType().getCategory();
        return category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || target.canBreatheUnderwater();
    }

    private static boolean isAerial(LivingEntity target) {
        return target instanceof FlyingAnimal
                || target instanceof ShoulderRidingEntity
                || (target instanceof Mob mob && mob.getNavigation() instanceof FlyingPathNavigation);
    }
}
