package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.util.SpawnUtils;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public class SpawnSetup {

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(EntityRegistry.OGRE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkOgreSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.CAVE_OGRE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkCaveOgreSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.WEREWOLF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkWerewolfSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.SILVER_SKELETON.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkSilverSkeletonSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.EVIL_BAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkEvilBatSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GHOST.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGhostSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GOLEM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGolemSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.OWLBEAR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkOwlbearSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.SHRIKE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkShrikeSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GOBLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGoblinSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.DART_GOBLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkDartGoblinSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.IMP.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkImpSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.HELL_OGRE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkHellOgreSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.SCORPION.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkScorpionSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.ICE_GOLEM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkIceGolemSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.FIRE_GHOST.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGhostSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.DESERT_SCORPION.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ScorpionEntity::checkScorpionSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.FROST_SCORPION.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ScorpionEntity::checkScorpionSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.NETHER_SCORPION.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ScorpionEntity::checkScorpionSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.HORSE_BAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkHorseBatSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.WILD_WOLF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkWildWolfSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.NIGHTMARE_HORSE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkNightmareHorseSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.SKELETON_HORSE_MOB.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkUndeadHorseSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(EntityRegistry.ZOMBIE_HORSE_MOB.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkUndeadHorseSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        // event.register(EntityRegistry.WEREVILLAGER.get(),
        //         SpawnPlacements.Type.ON_GROUND,
        //         Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        //         SpawnUtils::checkWerevillagerSpawnRules,
        //         SpawnPlacementRegisterEvent.Operation.OR);
    }
}
