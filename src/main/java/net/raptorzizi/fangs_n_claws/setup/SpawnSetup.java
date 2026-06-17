package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.util.SpawnUtils;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public class SpawnSetup {

    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(EntityRegistry.OGRE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkOgreSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.CAVE_OGRE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkCaveOgreSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.WEREWOLF.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkWerewolfSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.SILVER_SKELETON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkSilverSkeletonSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.SCORPION.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkScorpionSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.EVIL_BAT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkEvilBatSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GHOST.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGhostSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GOLEM.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGolemSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.OWLBEAR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkOwlbearSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GOBLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGoblinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.DART_GOBLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkDartGoblinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.IMP.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkImpSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        /*event.register(EntityRegistry.WEREVILLAGER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkWerevillagerSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);*/

        event.register(EntityRegistry.HELL_OGRE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkHellOgreSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.ICE_GOLEM.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkIceGolemSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.FIRE_GHOST.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnUtils::checkGhostSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
