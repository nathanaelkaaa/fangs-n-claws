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
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.DispenserBlock;
import net.raptorzizi.fangs_n_claws.entity.projectile.PoisonousDartEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
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
import net.raptorzizi.fangs_n_claws.item.armor.ScorpionArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.FurArmorItem;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.raptorzizi.fangs_n_claws.config.ClientConfigs;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.network.FangsNetwork;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.BabyOwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.shrike.ShrikeEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.undead_horse.SkeletonHorseMob;
import net.raptorzizi.fangs_n_claws.entity.undead_horse.ZombieHorseMob;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_ghost.FireGhostEntity;
import net.raptorzizi.fangs_n_claws.entity.horse_bat.HorseBatEntity;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.BabyScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.DesertScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.FrostScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.NetherScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.BiomeModifierRegistry;
import net.raptorzizi.fangs_n_claws.registries.EnchantmentsRegistry;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.BlocksRegistry;
import net.raptorzizi.fangs_n_claws.registries.CreativeModeTabs;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.MenuTypeRegistry;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import java.util.List;
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
        EnchantmentsRegistry.register(modEventBus);
        BiomeModifierRegistry.register(modEventBus);
        SoundsRegistry.register(modEventBus);
        ParticlesRegistry.register(modEventBus);
        MobEffectsRegistry.register(modEventBus);
        PotionsRegistry.register(modEventBus);
        BlocksRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        CreativeModeTabs.register(modEventBus);
        MenuTypeRegistry.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, FangsClawsMod.MOD_ID + "-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, FangsClawsMod.MOD_ID + "-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, FangsClawsMod.MOD_ID + "-common.toml");

        FangsNetwork.register();
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
            DispenserBlock.registerBehavior(ItemsRegistry.POISONOUS_DART.get(),
                    new AbstractProjectileDispenseBehavior() {
                        @Override
                        protected AbstractArrow getProjectile(Level level, Position pos, ItemStack stack) {
                            PoisonousDartEntity dart = new PoisonousDartEntity(EntityRegistry.POISONOUS_DART.get(), level);
                            dart.setPos(pos.x(), pos.y(), pos.z());
                            return dart;
                        }
                    });

            // Brewing recipes: BrewingRecipeRegistry.addRecipe(inputPotionIngredient, catalystIngredient, outputPotionStack)
            // Forge 1.20.1 uses addRecipe(Ingredient, Ingredient, ItemStack) — no addMix.
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(Potions.AWKWARD),
                    Ingredient.of(ItemsRegistry.SCORPION_STING.get()),
                    potionStack(PotionsRegistry.VENOM.get()));
            BrewingRecipeRegistry.addRecipe(
                    potionIngredient(PotionsRegistry.VENOM.get()),
                    Ingredient.of(Items.REDSTONE),
                    potionStack(PotionsRegistry.LONG_VENOM.get()));
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
                    potionIngredient(PotionsRegistry.NAUSEA.get()),
                    Ingredient.of(Items.REDSTONE),
                    potionStack(PotionsRegistry.LONG_NAUSEA.get()));
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
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        if (event.getEntity() instanceof SkeletonHorse vanilla) {
            if (serverLevel.getGameRules().getBoolean(GameRuleRegistry.VANILLA_SKELETON_HORSE)
                    || CommonConfigs.VANILLA_SKELETON_HORSE.get()) return; // garde le vanilla (gamerule OU config)
            SkeletonHorseMob mob = EntityRegistry.SKELETON_HORSE_MOB.get().create(serverLevel);
            if (mob != null) {
                mob.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
                mob.setTamed(vanilla.isTamed());
                mob.setTrap(vanilla.isTrap());
                serverLevel.addFreshEntity(mob);
                transferPassengers(vanilla, mob);
            }
            event.setCanceled(true);
        } else if (event.getEntity() instanceof ZombieHorse vanilla) {
            if (serverLevel.getGameRules().getBoolean(GameRuleRegistry.VANILLA_ZOMBIE_HORSE)
                    || CommonConfigs.VANILLA_ZOMBIE_HORSE.get()) return; // garde le vanilla (gamerule OU config)
            ZombieHorseMob mob = EntityRegistry.ZOMBIE_HORSE_MOB.get().create(serverLevel);
            if (mob != null) {
                mob.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
                mob.setTamed(vanilla.isTamed());
                serverLevel.addFreshEntity(mob);
                transferPassengers(vanilla, mob);
            }
            event.setCanceled(true);

        } else if (event.getEntity() instanceof SkeletonHorseMob mod
                && (serverLevel.getGameRules().getBoolean(GameRuleRegistry.VANILLA_SKELETON_HORSE)
                    || CommonConfigs.VANILLA_SKELETON_HORSE.get())) {
            SkeletonHorse vanilla = EntityType.SKELETON_HORSE.create(serverLevel);
            if (vanilla != null) {
                vanilla.moveTo(mod.getX(), mod.getY(), mod.getZ(), mod.getYRot(), mod.getXRot());
                vanilla.setTamed(mod.isTamed());
                vanilla.setTrap(mod.isTrap());
                serverLevel.addFreshEntity(vanilla);
                transferPassengers(mod, vanilla);
            }
            event.setCanceled(true);
        } else if (event.getEntity() instanceof ZombieHorseMob mod
                && (serverLevel.getGameRules().getBoolean(GameRuleRegistry.VANILLA_ZOMBIE_HORSE)
                    || CommonConfigs.VANILLA_ZOMBIE_HORSE.get())) {
            ZombieHorse vanilla = EntityType.ZOMBIE_HORSE.create(serverLevel);
            if (vanilla != null) {
                vanilla.moveTo(mod.getX(), mod.getY(), mod.getZ(), mod.getYRot(), mod.getXRot());
                vanilla.setTamed(mod.isTamed());
                serverLevel.addFreshEntity(vanilla);
                transferPassengers(mod, vanilla);
            }
            event.setCanceled(true);
        }
    }

    private static void transferPassengers(Entity from, Entity to) {
        for (Entity passenger : List.copyOf(from.getPassengers())) {
            passenger.stopRiding();
            passenger.startRiding(to, true);
        }
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

    private static boolean wearsScorpion(LivingEntity entity, EquipmentSlot slot) {
        return entity.getItemBySlot(slot).getItem() instanceof ScorpionArmorItem;
    }

    private static int countFurArmor(LivingEntity entity) {
        int n = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (entity.getItemBySlot(slot).getItem() instanceof FurArmorItem) n++;
        }
        return n;
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (entity.isInPowderSnow) {
            int furPieces = countFurArmor(entity);
            if (furPieces >= 4) {
                entity.setTicksFrozen(0);
            } else if (furPieces > 0 && entity.getRandom().nextInt(4) < furPieces) {
                entity.setTicksFrozen(Math.max(0, entity.getTicksFrozen() - 1));
            }
        }

        if (BleedingEffect.PENDING_REMOVAL.remove(entity.getUUID())) {
            entity.removeEffect(MobEffectsRegistry.BLEEDING.get());
        }

        if (entity instanceof PathfinderMob mob && mob.hasEffect(MobEffectsRegistry.VENOM.get())) {
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

        if (entity.tickCount % 20 == 0) {
            if (wearsScorpion(entity, EquipmentSlot.HEAD))  entity.removeEffect(MobEffects.CONFUSION);
            if (wearsScorpion(entity, EquipmentSlot.CHEST)) entity.removeEffect(MobEffects.POISON);
            if (wearsScorpion(entity, EquipmentSlot.LEGS))  entity.removeEffect(MobEffectsRegistry.VENOM.get());
            if (wearsScorpion(entity, EquipmentSlot.FEET))  entity.removeEffect(MobEffects.HUNGER);
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
    public void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffect effect = event.getEffectInstance().getEffect();
        boolean immune =
                   (effect == MobEffects.CONFUSION           && wearsScorpion(entity, EquipmentSlot.HEAD))
                || (effect == MobEffects.POISON              && wearsScorpion(entity, EquipmentSlot.CHEST))
                || (effect == MobEffectsRegistry.VENOM.get() && wearsScorpion(entity, EquipmentSlot.LEGS))
                || (effect == MobEffects.HUNGER              && wearsScorpion(entity, EquipmentSlot.FEET));
        if (immune) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Zombie && event.getNewTarget() instanceof WerevillagerEntity) {
            event.setCanceled(true);
        }
        if (event.getEntity() instanceof WildWolfEntity
                && event.getNewTarget() instanceof Player player
                && FurArmorItem.hasFurHelmet(player)) {
            event.setCanceled(true);
        }
        if (event.getNewTarget() != null
                && event.getEntity() instanceof PathfinderMob mob
                && mob.hasEffect(MobEffectsRegistry.VENOM.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        MobSpawnType type = event.getSpawnType();
        // MobSpawnEvent.FinalizeSpawn.getLevel() returns ServerLevelAccessor; get the actual ServerLevel
        ServerLevelAccessor accessor = event.getLevel();
        ServerLevel serverLevel = accessor.getLevel();

        var rules = serverLevel.getGameRules();
        Entity entity = event.getEntity();

        if (type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION) {
        boolean cancel = false;

        if (entity instanceof GoblinEntity           && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOBLIN))         cancel = true;
        else if (entity instanceof DartGoblinEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DART_GOBLIN)) cancel = true;
        else if (entity instanceof CaveOgreEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_CAVE_OGRE))     cancel = true;
        else if (entity instanceof OgreEntity     && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OGRE))           cancel = true;
        else if (entity instanceof IceGolemEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_ICE_GOLEM))      cancel = true;
        else if (entity instanceof GolemEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GOLEM))          cancel = true;
        else if (entity instanceof ShrikeEntity   && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SHRIKE))         cancel = true;
        else if (entity instanceof OwlbearEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_OWLBEAR))        cancel = true;
        else if (entity instanceof SilverSkeletonEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SILVER_SKELETON)) cancel = true;
        else if (entity instanceof EvilBatEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_EVIL_BAT))       cancel = true;
        else if (entity instanceof FireGhostEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_FIRE_GHOST))    cancel = true;
        else if (entity instanceof GhostEntity    && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_GHOST))          cancel = true;
        else if (entity instanceof WerewolfEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WEREWOLF))       cancel = true;
        else if (entity instanceof ImpEntity       && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_IMP))            cancel = true;
        else if (entity instanceof DesertScorpionEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DESERT_SCORPION)) cancel = true;
        else if (entity instanceof FrostScorpionEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_FROST_SCORPION))  cancel = true;
        else if (entity instanceof NetherScorpionEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_NETHER_SCORPION)) cancel = true;
        else if (entity instanceof ScorpionEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_SCORPION))       cancel = true;
        else if (entity instanceof NightmareHorseEntity && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_NIGHTMARE_HORSE)) cancel = true;
        else if (entity instanceof HorseBatEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_HORSE_BAT))      cancel = true;
        else if (entity instanceof WildWolfEntity  && !rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WILD_WOLF))      cancel = true;
        else if (entity instanceof SkeletonHorseMob && !rules.getBoolean(GameRuleRegistry.ALLOW_NATURAL_SPAWN_SKELETON_HORSE)) cancel = true;
        else if (entity instanceof ZombieHorseMob   && !rules.getBoolean(GameRuleRegistry.ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE))   cancel = true;

        if (cancel) event.setSpawnCancelled(true);

        if (entity instanceof GoblinEntity
                && !event.isSpawnCancelled()
                && rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_DART_GOBLIN)
                && serverLevel.random.nextInt(25) == 0) {

            DartGoblinEntity dartGoblin = EntityRegistry.DART_GOBLIN.get().create(serverLevel);
            if (dartGoblin != null) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 6.0;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 6.0;
                dartGoblin.moveTo(
                        entity.getX() + offsetX,
                        entity.getY(),
                        entity.getZ() + offsetZ,
                        serverLevel.random.nextFloat() * 360f, 0f);
                dartGoblin.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(dartGoblin.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                serverLevel.addFreshEntity(dartGoblin);
            }
        }

        if (entity instanceof OwlbearEntity owlbearParent && !event.isSpawnCancelled()) {
            float roll = serverLevel.random.nextFloat();
            int babies = roll < 0.05f ? 2 : (roll < 0.35f ? 1 : 0);
            boolean parentIsShrike = owlbearParent instanceof ShrikeEntity;
            for (int i = 0; i < babies; i++) {
                BabyOwlbearEntity baby = parentIsShrike
                        ? EntityRegistry.BABY_SHRIKE.get().create(serverLevel)
                        : EntityRegistry.BABY_OWLBEAR.get().create(serverLevel);
                if (baby != null) {
                    double ox = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                    double oz = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                    baby.moveTo(owlbearParent.getX() + ox, owlbearParent.getY(), owlbearParent.getZ() + oz,
                            serverLevel.random.nextFloat() * 360f, 0f);
                    baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, null, null);
                    baby.setParent(owlbearParent);
                    serverLevel.addFreshEntity(baby);
                }
            }
        }

        if (entity instanceof ScorpionEntity scorpionParent && !event.isSpawnCancelled()) {
            float roll = serverLevel.random.nextFloat();
            int babies = roll < 0.05f ? 2 : (roll < 0.35f ? 1 : 0);
            int variant = BabyScorpionEntity.variantOf(scorpionParent);
            for (int i = 0; i < babies; i++) {
                BabyScorpionEntity baby = EntityRegistry.BABY_SCORPION.get().create(serverLevel);
                if (baby != null) {
                    baby.setVariant(variant);
                    double ox = (serverLevel.random.nextDouble() - 0.5) * 2.5;
                    double oz = (serverLevel.random.nextDouble() - 0.5) * 2.5;
                    baby.moveTo(scorpionParent.getX() + ox, scorpionParent.getY(), scorpionParent.getZ() + oz,
                            serverLevel.random.nextFloat() * 360f, 0f);
                    baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, null, null);
                    baby.setParent(scorpionParent);
                    baby.setRideYaw(serverLevel.random.nextFloat() * 360f);
                    serverLevel.addFreshEntity(baby);
                    baby.startRiding(scorpionParent, false);
                }
            }
        }
        }

        if (entity instanceof GoblinEntity goblin
                && !event.isSpawnCancelled()
                && !goblin.isPassenger()
                && rules.getBoolean(GameRuleRegistry.ALLOW_SPAWN_WILD_WOLF)
                && serverLevel.random.nextInt(20) == 0) {  // ~5% : goblin-cavalier sur un loup (main avait nextInt(1) = 100%, valeur de debug)

            WildWolfEntity wolf = EntityRegistry.WILD_WOLF.get().create(serverLevel);
            if (wolf != null) {
                wolf.moveTo(goblin.getX(), goblin.getY(), goblin.getZ(), goblin.getYRot(), 0f);
                wolf.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(wolf.blockPosition()),
                        MobSpawnType.JOCKEY, null, null);
                serverLevel.addFreshEntity(wolf);
                goblin.startRiding(wolf, true);
            }
        }
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(FangsClawsMod.MOD_ID, path);
    }

}
