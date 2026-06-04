package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.evil_eye.EvilEyeProjectile;
import net.raptorzizi.fangs_n_claws.entity.velocity_arrow.VelocityArrowEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookEntity;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;

public class EntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, FangsClawsMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<GolemEntity>> GOLEM =
            ENTITIES.register("golem", () -> EntityType.Builder.of(GolemEntity::new, MobCategory.MONSTER)
                    .sized(2F, 2.6F)
                    .build("golem"));

    public static final RegistryObject<EntityType<OgreEntity>> OGRE =
            ENTITIES.register("ogre", () -> EntityType.Builder.of(OgreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.95F)
                    .build("ogre"));

    public static final RegistryObject<EntityType<CaveOgreEntity>> CAVE_OGRE =
            ENTITIES.register("cave_ogre", () -> EntityType.Builder.of(CaveOgreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.95F)
                    .build("cave_ogre"));

    public static final RegistryObject<EntityType<WerewolfEntity>> WEREWOLF =
            ENTITIES.register("werewolf", () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.0F)
                    .build("werewolf"));

    public static final RegistryObject<EntityType<OwlbearEntity>> OWLBEAR =
            ENTITIES.register("owlbear", () -> EntityType.Builder.of(OwlbearEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .build("owlbear"));

    public static final RegistryObject<EntityType<SilverSkeletonEntity>> SILVER_SKELETON =
            ENTITIES.register("silver_skeleton", () -> EntityType.Builder.of(SilverSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.4F)
                    .build("silver_skeleton"));

    public static final RegistryObject<EntityType<EvilBatEntity>> EVIL_BAT =
            ENTITIES.register("evil_bat", () -> EntityType.Builder.of(EvilBatEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F)
                    .clientTrackingRange(5)
                    .build("evil_bat"));

    public static final RegistryObject<EntityType<GhostEntity>> GHOST =
            ENTITIES.register("ghost", () -> EntityType.Builder.of(GhostEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 1.6F)
                    .clientTrackingRange(8)
                    .build("ghost"));

    public static final RegistryObject<EntityType<GoblinEntity>> GOBLIN =
            ENTITIES.register("goblin", () -> EntityType.Builder.of(GoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.4F)
                    .clientTrackingRange(8)
                    .build("goblin"));

    public static final RegistryObject<EntityType<DartGoblinEntity>> DART_GOBLIN =
            ENTITIES.register("dart_goblin", () -> EntityType.Builder.of(DartGoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.4F)
                    .clientTrackingRange(8)
                    .build("dart_goblin"));

    public static final RegistryObject<EntityType<PoisonousDartEntity>> POISONOUS_DART =
            ENTITIES.register("poisonous_dart", () -> EntityType.Builder
                    .<PoisonousDartEntity>of(PoisonousDartEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("poisonous_dart"));

    public static final RegistryObject<EntityType<WerevillagerEntity>> WEREVILLAGER =
            ENTITIES.register("werevillager", () -> EntityType.Builder
                    .<WerevillagerEntity>of((type, level) -> new WerevillagerEntity(type, level), MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("werevillager"));

    public static final RegistryObject<EntityType<CatchingClawHookEntity>> CATCHING_CLAW_HOOK =
            ENTITIES.register("catching_claw_hook", () -> EntityType.Builder
                    .<CatchingClawHookEntity>of(CatchingClawHookEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("catching_claw_hook"));

    public static final RegistryObject<EntityType<NetheriteClawHookEntity>> NETHERITE_CLAW_HOOK =
            ENTITIES.register("netherite_claw_hook", () -> EntityType.Builder
                    .<NetheriteClawHookEntity>of(NetheriteClawHookEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("netherite_claw_hook"));

    public static final RegistryObject<EntityType<EvilEyeProjectile>> EVIL_EYE_PROJECTILE =
            ENTITIES.register("evil_eye_projectile", () -> EntityType.Builder
                    .<EvilEyeProjectile>of(EvilEyeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .build("evil_eye_projectile"));

    public static final RegistryObject<EntityType<VelocityArrowEntity>> VELOCITY_ARROW_ENTITY =
            ENTITIES.register("velocity_arrow", () -> EntityType.Builder
                    .<VelocityArrowEntity>of(VelocityArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("velocity_arrow"));

    public static final RegistryObject<EntityType<BlockProjectile>> BLOCK_PROJECTILE =
            ENTITIES.register("block_projectile", () -> EntityType.Builder
                    .<BlockProjectile>of(BlockProjectile::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(64)
                    .build("block_projectile"));
}