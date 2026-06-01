package net.raptorzizi.fangs_n_claws;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
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
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
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
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, ItemsRegistry.EVIL_EYE.get(), PotionsRegistry.BLINDNESS);
        builder.addMix(PotionsRegistry.BLINDNESS, Items.REDSTONE, PotionsRegistry.LONG_BLINDNESS);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.GIANT_FEATHER.get(), Potions.STRONG_SWIFTNESS);
        builder.addMix(Potions.AWKWARD, ItemsRegistry.VILE_FAT.get(), PotionsRegistry.NAUSEA);
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
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Zombie && event.getNewAboutToBeSetTarget() instanceof WerevillagerEntity) {
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
        else if (event.getEntity() instanceof CaveOgreEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_CAVE_OGRE))     cancel = true;
        else if (event.getEntity() instanceof OgreEntity     && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OGRE))           cancel = true;
        else if (event.getEntity() instanceof GolemEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOLEM))          cancel = true;
        else if (event.getEntity() instanceof OwlbearEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OWLBEAR))        cancel = true;
        else if (event.getEntity() instanceof SilverSkeletonEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SILVER_SKELETON)) cancel = true;
        else if (event.getEntity() instanceof EvilBatEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_EVIL_BAT))       cancel = true;
        else if (event.getEntity() instanceof GhostEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GHOST))          cancel = true;
        else if (event.getEntity() instanceof WerewolfEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WEREWOLF))       cancel = true;

        if (cancel) event.setSpawnCancelled(true);
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, path);
    }

}
