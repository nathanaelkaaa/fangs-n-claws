package net.raptorzizi.fangs_n_claws;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.raptorzizi.fangs_n_claws.network.TotemFrostPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.raptorzizi.fangs_n_claws.item.armor.ScorpionArmorItem;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.raptorzizi.fangs_n_claws.item.CatchingClawItem;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.raptorzizi.fangs_n_claws.config.ClientConfigs;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.raptorzizi.fangs_n_claws.entity.fire_ghost.FireGhostEntity;
import net.raptorzizi.fangs_n_claws.entity.horse_bat.HorseBatEntity;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import net.raptorzizi.fangs_n_claws.util.SpawnUtils;
import net.raptorzizi.fangs_n_claws.entity.scorpion.DesertScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.FrostScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.NetherScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.registries.BiomeModifierRegistry;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import net.raptorzizi.fangs_n_claws.registries.CreativeModeTabs;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.GameRuleRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.PotionsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(FangsClawsMod.MOD_ID)
public class FangsClawsMod {
    public static final String MOD_ID = "fangs_n_claws";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FangsClawsMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener((RegisterPayloadHandlersEvent e) ->
                e.registrar(MOD_ID).playToClient(
                    TotemFrostPayload.TYPE,
                    TotemFrostPayload.STREAM_CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() ->
                        net.minecraft.client.Minecraft.getInstance().gameRenderer.displayItemActivation(payload.item())
                    )
                )
            );
        }

        NeoForge.EVENT_BUS.register(this);

        ItemsRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        BiomeModifierRegistry.register(modEventBus);
        SoundsRegistry.register(modEventBus);
        ParticlesRegistry.register(modEventBus);
        MobEffectsRegistry.register(modEventBus);
        PotionsRegistry.register(modEventBus);
        BlocksRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        CreativeModeTabs.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", FangsClawsMod.MOD_ID));
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", FangsClawsMod.MOD_ID));
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, String.format("%s-common.toml", FangsClawsMod.MOD_ID));

        GameRuleRegistry.init();

        modEventBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            event.register(EntityRegistry.DESERT_SCORPION.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ScorpionEntity::checkScorpionSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.FROST_SCORPION.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ScorpionEntity::checkScorpionSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.NETHER_SCORPION.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ScorpionEntity::checkScorpionSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.HORSE_BAT.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnUtils::checkHorseBatSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.NIGHTMARE_HORSE.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnUtils::checkNightmareHorseSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.WILD_WOLF.get(),
                    SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnUtils::checkWildWolfSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
            DispenserBlock.registerBehavior(ItemsRegistry.POISONOUS_DART.get(),
                    new ProjectileDispenseBehavior(ItemsRegistry.POISONOUS_DART.get()))
        );
    }

    @SubscribeEvent
    public void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, ItemsRegistry.SCORPION_STING.get(), PotionsRegistry.VENOM);
        builder.addMix(PotionsRegistry.VENOM, Items.REDSTONE, PotionsRegistry.LONG_VENOM);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.EVIL_EYE.get(), PotionsRegistry.BLINDNESS);
        builder.addMix(PotionsRegistry.BLINDNESS, Items.REDSTONE, PotionsRegistry.LONG_BLINDNESS);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.GIANT_FEATHER.get(), Potions.STRONG_SWIFTNESS);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.VILE_FAT.get(), PotionsRegistry.NAUSEA);
        builder.addMix(PotionsRegistry.NAUSEA, Items.REDSTONE, PotionsRegistry.LONG_NAUSEA);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.SPECTRAL_ESSENCE.get(), Potions.INVISIBILITY);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player
                && player.getMainHandItem().getItem() instanceof CatchingClawItem) {
            event.setAmount(1.0f);
        }
    }

    @SubscribeEvent
    public void onKnockBack(LivingKnockBackEvent event) {
        if (BleedingEffect.BLEEDING_DAMAGE_ACTIVE.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEffectParticle(EffectParticleModificationEvent event) {
        if (event.getEffect().getEffect().is(FangsClawsMod.id("bleeding"))) {
            event.setVisible(false);
        }
        if (event.getEffect().getEffect().is(FangsClawsMod.id("stunned"))) {
            event.setVisible(false);
        }
    }

    @SubscribeEvent
    public void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        if (BleedingEffect.PENDING_REMOVAL.remove(entity.getUUID())) {
            entity.removeEffect(MobEffectsRegistry.BLEEDING);
        }

        if (entity instanceof PathfinderMob mob && mob.hasEffect(MobEffectsRegistry.VENOM)) {
            if (mob.getTarget() != null) mob.setTarget(null);
            if (mob.tickCount % 10 == 0) {
                double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
                double dist  = 4.0 + mob.getRandom().nextDouble() * 4.0;
                Vec3 randomTarget = mob.position().add(
                        Math.sin(angle) * dist,
                        0.0,
                        Math.cos(angle) * dist);
                mob.getNavigation().moveTo(randomTarget.x, randomTarget.y, randomTarget.z, 1.4);
            }
        }

        if (entity instanceof Player player && player.tickCount % 20 == 0) {
            boolean fullSet = player.getItemBySlot(EquipmentSlot.HEAD).getItem()  instanceof ScorpionArmorItem
                           && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ScorpionArmorItem
                           && player.getItemBySlot(EquipmentSlot.LEGS).getItem()  instanceof ScorpionArmorItem
                           && player.getItemBySlot(EquipmentSlot.FEET).getItem()  instanceof ScorpionArmorItem;
            if (fullSet) {
                player.addEffect(new MobEffectInstance(MobEffectsRegistry.MITHRIDATIC, 40, 0, false, false, true));
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.HUNGER);
                player.removeEffect(MobEffectsRegistry.VENOM);
            }
        }

        if (entity instanceof Monster monster && monster.tickCount % 15 == 0) {
            Level level = monster.level();
            BlockPos mobPos = monster.blockPosition();
            int radius = 6;

            BlockPos nearestLantern = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (BlockPos pos : BlockPos.betweenClosed(
                    mobPos.offset(-radius, -3, -radius),
                    mobPos.offset( radius,  3,  radius))) {
                if (level.getBlockState(pos).is(BlocksRegistry.VILE_LANTERN.get())) {
                    double d = pos.distSqr(mobPos);
                    if (d < nearestDistSq) {
                        nearestDistSq = d;
                        nearestLantern = pos.immutable();
                    }
                }
            }

            if (nearestLantern != null) {
                Vec3 away = monster.position()
                        .subtract(Vec3.atCenterOf(nearestLantern))
                        .normalize()
                        .scale(0.5);
                monster.setDeltaMovement(monster.getDeltaMovement().add(away));
                monster.hasImpulse = true;
                if (monster instanceof PathfinderMob pathMob) {
                    Vec3 fleeTarget = monster.position().add(away.scale(12));
                    pathMob.getNavigation().moveTo(
                            fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEntity().hasEffect(MobEffectsRegistry.MITHRIDATIC)) return;

        var effect = event.getEffectInstance().getEffect();
        if (effect.is(MobEffects.POISON)
                || effect.is(MobEffects.CONFUSION)
                || effect.is(MobEffects.BLINDNESS)
                || effect.is(MobEffects.HUNGER)
                || effect.is(MobEffectsRegistry.VENOM)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Zombie && event.getNewAboutToBeSetTarget() instanceof WerevillagerEntity) {
            event.setCanceled(true);
        }
        if (event.getNewAboutToBeSetTarget() != null
                && event.getEntity() instanceof PathfinderMob mob
                && mob.hasEffect(MobEffectsRegistry.VENOM)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        var rules = serverLevel.getGameRules();
        boolean cancel = false;

        if (event.getEntity() instanceof GoblinEntity        && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOBLIN))         cancel = true;
        else if (event.getEntity() instanceof DartGoblinEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DART_GOBLIN)) cancel = true;
        else if (event.getEntity() instanceof CaveOgreEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_CAVE_OGRE))     cancel = true;
        else if (event.getEntity() instanceof OgreEntity     && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OGRE))           cancel = true;
        else if (event.getEntity() instanceof IceGolemEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_ICE_GOLEM))      cancel = true;
        else if (event.getEntity() instanceof GolemEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOLEM))          cancel = true;
        else if (event.getEntity() instanceof OwlbearEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OWLBEAR))        cancel = true;
        else if (event.getEntity() instanceof SilverSkeletonEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SILVER_SKELETON)) cancel = true;
        else if (event.getEntity() instanceof EvilBatEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_EVIL_BAT))       cancel = true;
        else if (event.getEntity() instanceof FireGhostEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_FIRE_GHOST))    cancel = true;
        else if (event.getEntity() instanceof GhostEntity      && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GHOST))         cancel = true;
        else if (event.getEntity() instanceof WerewolfEntity   && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WEREWOLF))      cancel = true;
        else if (event.getEntity() instanceof ImpEntity        && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_IMP))           cancel = true;
        else if (event.getEntity() instanceof DesertScorpionEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DESERT_SCORPION)) cancel = true;
        else if (event.getEntity() instanceof FrostScorpionEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_FROST_SCORPION))  cancel = true;
        else if (event.getEntity() instanceof NetherScorpionEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_NETHER_SCORPION)) cancel = true;
        else if (event.getEntity() instanceof ScorpionEntity   && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SCORPION))      cancel = true;
        else if (event.getEntity() instanceof NightmareHorseEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_NIGHTMARE_HORSE)) cancel = true;
        else if (event.getEntity() instanceof HorseBatEntity   && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_HORSE_BAT))     cancel = true;
        else if (event.getEntity() instanceof WildWolfEntity   && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WILD_WOLF))    cancel = true;

        if (cancel) event.setSpawnCancelled(true);

        if (event.getEntity() instanceof GoblinEntity
                && !event.isSpawnCancelled()
                && rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DART_GOBLIN)
                && serverLevel.random.nextInt(25) == 0) {

            DartGoblinEntity dartGoblin = EntityRegistry.DART_GOBLIN.get().create(serverLevel);
            if (dartGoblin != null) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 6.0;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 6.0;
                dartGoblin.moveTo(
                        event.getEntity().getX() + offsetX,
                        event.getEntity().getY(),
                        event.getEntity().getZ() + offsetZ,
                        serverLevel.random.nextFloat() * 360f, 0f);
                dartGoblin.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(dartGoblin.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null);
                serverLevel.addFreshEntity(dartGoblin);
            }
        }
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, path);
    }

}
