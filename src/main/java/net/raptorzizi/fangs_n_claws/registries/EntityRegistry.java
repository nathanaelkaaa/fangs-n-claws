package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.evil_eye.EvilEyeProjectile;
import net.raptorzizi.fangs_n_claws.entity.velocity_arrow.VelocityArrowEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_ghost.FireGhostEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.DesertScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.FrostScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.NetherScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookEntity;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.HellFirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.frozen_box.FrozenBoxEntity;
import net.raptorzizi.fangs_n_claws.entity.horse_bat.HorseBatEntity;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;

public class EntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, FangsClawsMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final DeferredHolder<EntityType<?>, EntityType<GolemEntity>> GOLEM =
            ENTITIES.register("golem", () -> EntityType.Builder.of(GolemEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.6F)
                    .build("golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<IceGolemEntity>> ICE_GOLEM =
            ENTITIES.register("ice_golem", () -> EntityType.Builder.of(IceGolemEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.6F)
                    .build("ice_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<OgreEntity>> OGRE =
            ENTITIES.register("ogre", () -> EntityType.Builder.of(OgreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.95F)
                    .build("ogre"));

    public static final DeferredHolder<EntityType<?>, EntityType<CaveOgreEntity>> CAVE_OGRE =
            ENTITIES.register("cave_ogre", () -> EntityType.Builder.of(CaveOgreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.95F)
                    .build("cave_ogre"));

    public static final DeferredHolder<EntityType<?>, EntityType<HellOgreEntity>> HELL_OGRE =
            ENTITIES.register("hell_ogre", () -> EntityType.Builder.of(HellOgreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.95F)
                    .fireImmune()
                    .build("hell_ogre"));

    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF =
            ENTITIES.register("werewolf", () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.95F)
                    .build("werewolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<OwlbearEntity>> OWLBEAR =
            ENTITIES.register("owlbear", () -> EntityType.Builder.of(OwlbearEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 1.95F)
                    .build("owlbear"));

    public static final DeferredHolder<EntityType<?>, EntityType<SilverSkeletonEntity>> SILVER_SKELETON =
            ENTITIES.register("silver_skeleton", () -> EntityType.Builder.of(SilverSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build("silver_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<EvilBatEntity>> EVIL_BAT =
            ENTITIES.register("evil_bat", () -> EntityType.Builder.of(EvilBatEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F)
                    .clientTrackingRange(5)
                    .build("evil_bat"));

    public static final DeferredHolder<EntityType<?>, EntityType<GhostEntity>> GHOST =
            ENTITIES.register("ghost", () -> EntityType.Builder.of(GhostEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 1.6F)
                    .clientTrackingRange(8)
                    .build("ghost"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireGhostEntity>> FIRE_GHOST =
            ENTITIES.register("fire_ghost", () -> EntityType.Builder.of(FireGhostEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 1.6F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build("fire_ghost"));

    public static final DeferredHolder<EntityType<?>, EntityType<FirePitchforkEntity>> FIRE_PITCHFORk_ENTITY =
            ENTITIES.register("fire_pitchfork", () -> EntityType.Builder
                    .<FirePitchforkEntity>of(FirePitchforkEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("fire_pitchfork"));

    public static final DeferredHolder<EntityType<?>, EntityType<HellFirePitchforkEntity>> HELLFIRE_PITCHFORK_ENTITY =
            ENTITIES.register("hellfire_pitchfork", () -> EntityType.Builder
                    .<HellFirePitchforkEntity>of(HellFirePitchforkEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("hellfire_pitchfork"));

    public static final DeferredHolder<EntityType<?>, EntityType<ImpEntity>> IMP =
            ENTITIES.register("imp", () -> EntityType.Builder.of(ImpEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 1.2F)
                    .clientTrackingRange(8)
                    .build("imp"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoblinEntity>> GOBLIN =
            ENTITIES.register("goblin", () -> EntityType.Builder.of(GoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.4F)
                    .clientTrackingRange(8)
                    .build("goblin"));

    public static final DeferredHolder<EntityType<?>, EntityType<DartGoblinEntity>> DART_GOBLIN =
            ENTITIES.register("dart_goblin", () -> EntityType.Builder.of(DartGoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.4F)
                    .clientTrackingRange(8)
                    .build("dart_goblin"));

    public static final DeferredHolder<EntityType<?>, EntityType<PoisonousDartEntity>> POISONOUS_DART =
            ENTITIES.register("poisonous_dart", () -> EntityType.Builder
                    .<PoisonousDartEntity>of(PoisonousDartEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("poisonous_dart"));

    public static final DeferredHolder<EntityType<?>, EntityType<WerevillagerEntity>> WEREVILLAGER =
            ENTITIES.register("werevillager", () -> EntityType.Builder
                    .<WerevillagerEntity>of((type, level) -> new WerevillagerEntity(type, level), MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("werevillager"));

    public static final DeferredHolder<EntityType<?>, EntityType<CatchingClawHookEntity>> CATCHING_CLAW_HOOK =
            ENTITIES.register("catching_claw_hook", () -> EntityType.Builder
                    .<CatchingClawHookEntity>of(CatchingClawHookEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(5)
                    .build("catching_claw_hook"));

    public static final DeferredHolder<EntityType<?>, EntityType<NetheriteClawHookEntity>> NETHERITE_CLAW_HOOK =
            ENTITIES.register("netherite_claw_hook", () -> EntityType.Builder
                    .<NetheriteClawHookEntity>of(NetheriteClawHookEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(5)
                    .build("netherite_claw_hook"));

    public static final DeferredHolder<EntityType<?>, EntityType<EvilEyeProjectile>> EVIL_EYE_PROJECTILE =
            ENTITIES.register("evil_eye_projectile", () -> EntityType.Builder
                    .<EvilEyeProjectile>of(EvilEyeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .build("evil_eye_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<VelocityArrowEntity>> VELOCITY_ARROW_ENTITY =
            ENTITIES.register("velocity_arrow", () -> EntityType.Builder
                    .<VelocityArrowEntity>of(VelocityArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("velocity_arrow"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlockProjectile>> BLOCK_PROJECTILE =
            ENTITIES.register("block_projectile", () -> EntityType.Builder
                    .<BlockProjectile>of(BlockProjectile::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(64)
                    .build("block_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<ScorpionEntity>> SCORPION =
            ENTITIES.register("scorpion", () -> EntityType.Builder
                    .<ScorpionEntity>of((type, level) -> new ScorpionEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(8)
                    .build("scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<DesertScorpionEntity>> DESERT_SCORPION =
            ENTITIES.register("desert_scorpion", () -> EntityType.Builder
                    .<DesertScorpionEntity>of((type, level) -> new DesertScorpionEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(8)
                    .build("desert_scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<FrostScorpionEntity>> FROST_SCORPION =
            ENTITIES.register("frost_scorpion", () -> EntityType.Builder
                    .<FrostScorpionEntity>of((type, level) -> new FrostScorpionEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(8)
                    .build("frost_scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<NetherScorpionEntity>> NETHER_SCORPION =
            ENTITIES.register("nether_scorpion", () -> EntityType.Builder
                    .<NetherScorpionEntity>of((type, level) -> new NetherScorpionEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build("nether_scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<HorseBatEntity>> HORSE_BAT =
            ENTITIES.register("horse_bat", () -> EntityType.Builder
                    .<HorseBatEntity>of((type, level) -> new HorseBatEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 1.6F)
                    .clientTrackingRange(8)
                    .build("horse_bat"));

    public static final DeferredHolder<EntityType<?>, EntityType<NightmareHorseEntity>> NIGHTMARE_HORSE =
            ENTITIES.register("nightmare_horse", () -> EntityType.Builder
                    .<NightmareHorseEntity>of((type, level) -> new NightmareHorseEntity(type, level), MobCategory.MONSTER)
                    .sized(1.4F, 1.6F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .build("nightmare_horse"));

    public static final DeferredHolder<EntityType<?>, EntityType<WildWolfEntity>> WILD_WOLF =
            ENTITIES.register("wild_wolf", () -> EntityType.Builder
                    .<WildWolfEntity>of((type, level) -> new WildWolfEntity(type, level), MobCategory.MONSTER)
                    .sized(0.78F, 1.105F)
                    .clientTrackingRange(10)
                    .build("wild_wolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<FrozenBoxEntity>> FROZEN_BOX =
            ENTITIES.register("frozen_box", () -> EntityType.Builder
                    .<FrozenBoxEntity>of(FrozenBoxEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("frozen_box"));
}