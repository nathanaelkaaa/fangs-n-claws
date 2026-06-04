package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraft.world.entity.monster.Monster;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public class SpawnSetup {

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(EntityRegistry.OGRE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.WEREWOLF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WerewolfEntity::checkWerewolfSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.SILVER_SKELETON.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.EVIL_BAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EvilBatEntity::checkEvilBatSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GHOST.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GhostEntity::checkGhostSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GOLEM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GolemEntity::checkGolemSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.OWLBEAR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OwlbearEntity::checkOwlbearSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.GOBLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GoblinEntity::checkGoblinSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);

        event.register(EntityRegistry.WEREVILLAGER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WerevillagerEntity::checkWerevillagerSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
}
