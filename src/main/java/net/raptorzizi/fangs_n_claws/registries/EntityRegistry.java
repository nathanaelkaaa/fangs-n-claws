package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;

public class EntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, FangsClawsMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final DeferredHolder<EntityType<?>, EntityType<GolemEntity>> GOLEM =
            ENTITIES.register("golem", () -> EntityType.Builder.of(GolemEntity::new, MobCategory.MONSTER)
                    .sized(2F, 2.6F)
                    .build("golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<OgreEntity>> OGRE =
            ENTITIES.register("ogre", () -> EntityType.Builder.of(OgreEntity::new, MobCategory.MONSTER)
                    .sized(2F, 2.8F)
                    .build("ogre"));

    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF =
            ENTITIES.register("werewolf", () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.0F)
                    .build("werewolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<OwlbearEntity>> OWLBEAR =
            ENTITIES.register("owlbear", () -> EntityType.Builder.of(OwlbearEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .build("owlbear"));

    public static final DeferredHolder<EntityType<?>, EntityType<SilverSkeletonEntity>> SILVER_SKELETON =
            ENTITIES.register("silver_skeleton", () -> EntityType.Builder.of(SilverSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.4F)
                    .build("silver_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<EvilBatEntity>> EVIL_BAT =
            ENTITIES.register("evil_bat", () -> EntityType.Builder.of(EvilBatEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F)
                    .clientTrackingRange(5)
                    .build("evil_bat"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlockProjectile>> BLOCK_PROJECTILE =
            ENTITIES.register("block_projectile", () -> EntityType.Builder
                    .<BlockProjectile>of(BlockProjectile::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(64)
                    .build("block_projectile"));
}