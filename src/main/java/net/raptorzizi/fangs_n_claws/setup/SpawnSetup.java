package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public class SpawnSetup {

    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(EntityRegistry.OGRE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.WEREWOLF.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.SILVER_SKELETON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.EVIL_BAT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EvilBatEntity::checkEvilBatSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GHOST.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GhostEntity::checkGhostSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GOLEM.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GolemEntity::checkGolemSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.OWLBEAR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OwlbearEntity::checkOwlbearSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.GOBLIN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GoblinEntity::checkGoblinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(EntityRegistry.WEREVILLAGER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WerevillagerEntity::checkWerevillagerSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
