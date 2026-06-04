package net.raptorzizi.fangs_n_claws;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.raptorzizi.fangs_n_claws.item.CatchingClawItem;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.raptorzizi.fangs_n_claws.config.ClientConfigs;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
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

    public FangsClawsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

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

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, FangsClawsMod.MOD_ID + "-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, FangsClawsMod.MOD_ID + "-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, FangsClawsMod.MOD_ID + "-common.toml");

        GameRuleRegistry.init();
    }

    private static ItemStack potionStack(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    private static Ingredient potionIngredient(Potion potion) {
        return Ingredient.of(potionStack(potion));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Brewing recipes: BrewingRecipeRegistry.addRecipe(inputPotionIngredient, catalystIngredient, outputPotionStack)
            // Forge 1.20.1 uses addRecipe(Ingredient, Ingredient, ItemStack) — no addMix.
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(Potions.AWKWARD),
                    Ingredient.of(ItemsRegistry.EVIL_EYE.get()),
                    potionStack(PotionsRegistry.BLINDNESS.get()));
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(PotionsRegistry.BLINDNESS.get()),
                    Ingredient.of(Items.REDSTONE),
                    potionStack(PotionsRegistry.LONG_BLINDNESS.get()));
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(Potions.AWKWARD),
                    Ingredient.of(ItemsRegistry.GIANT_FEATHER.get()),
                    potionStack(Potions.STRONG_SWIFTNESS));
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(Potions.AWKWARD),
                    Ingredient.of(ItemsRegistry.VILE_FAT.get()),
                    potionStack(PotionsRegistry.NAUSEA.get()));
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(Potions.AWKWARD),
                    Ingredient.of(ItemsRegistry.SPECTRAL_ESSENCE.get()),
                    potionStack(Potions.INVISIBILITY));
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onIncomingDamage(LivingHurtEvent event) {
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

    // TODO 1.20.1: EffectParticleModificationEvent does not exist in NeoForge 1.20.1.
    // Bleeding and Stunned effects will show vanilla particles. Remove when backporting is complete or find alternative.
    // @SubscribeEvent
    // public void onEffectParticle(EffectParticleModificationEvent event) {
    //     if (event.getEffect().getEffect().is(FangsClawsMod.id("bleeding"))) { event.setVisible(false); }
    //     if (event.getEffect().getEffect().is(FangsClawsMod.id("stunned")))  { event.setVisible(false); }
    // }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (BleedingEffect.PENDING_REMOVAL.remove(entity.getUUID())) {
            entity.removeEffect(MobEffectsRegistry.BLEEDING.get());
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
                // Monster extends PathfinderMob; direct cast is safe here
                Vec3 fleeTarget = monster.position().add(away.scale(12));
                monster.getNavigation().moveTo(
                        fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5);
            }
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Zombie && event.getNewTarget() instanceof WerevillagerEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION) return;
        // MobSpawnEvent.FinalizeSpawn.getLevel() returns ServerLevelAccessor; get the actual ServerLevel
        ServerLevelAccessor accessor = event.getLevel();
        ServerLevel serverLevel = accessor.getLevel();

        var rules = serverLevel.getGameRules();
        boolean cancel = false;
        Entity entity = event.getEntity();

        if (entity instanceof GoblinEntity        && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOBLIN))         cancel = true;
        else if (entity instanceof CaveOgreEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_CAVE_OGRE))     cancel = true;
        else if (entity instanceof OgreEntity     && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OGRE))           cancel = true;
        else if (entity instanceof GolemEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOLEM))          cancel = true;
        else if (entity instanceof OwlbearEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OWLBEAR))        cancel = true;
        else if (entity instanceof SilverSkeletonEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SILVER_SKELETON)) cancel = true;
        else if (entity instanceof EvilBatEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_EVIL_BAT))       cancel = true;
        else if (entity instanceof GhostEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GHOST))          cancel = true;
        else if (entity instanceof WerewolfEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WEREWOLF))       cancel = true;

        if (cancel) event.setSpawnCancelled(true);
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, path);
    }

}
