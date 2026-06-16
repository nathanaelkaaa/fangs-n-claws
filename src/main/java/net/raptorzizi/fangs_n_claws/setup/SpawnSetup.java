package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
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

        // event.register(EntityRegistry.WEREVILLAGER.get(),
        //         SpawnPlacements.Type.ON_GROUND,
        //         Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        //         SpawnUtils::checkWerevillagerSpawnRules,
        //         SpawnPlacementRegisterEvent.Operation.OR);
    }
}
