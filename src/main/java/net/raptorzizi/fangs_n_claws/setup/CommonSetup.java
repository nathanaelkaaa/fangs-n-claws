package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.entity.shrike.ShrikeEntity;
import net.raptorzizi.fangs_n_claws.network.TotemFrostPayload;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionHand;
import java.util.List;
import net.minecraft.world.InteractionResult;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.item.armor.OwlArmorItem;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.raptorzizi.fangs_n_claws.effect.FrozenEffect;
import net.raptorzizi.fangs_n_claws.entity.frozen_box.FrozenBoxEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.mimic.MimicEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_ghost.FireGhostEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.BabyOwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.BabyScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.horse_bat.HorseBatEntity;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;
import net.raptorzizi.fangs_n_claws.entity.undead_horse.SkeletonHorseMob;
import net.raptorzizi.fangs_n_claws.entity.undead_horse.ZombieHorseMob;
import net.raptorzizi.fangs_n_claws.entity.carnivorous_plant.CarnivorousPlantSproutEntity;
import net.raptorzizi.fangs_n_claws.entity.carnivorous_plant.CarnivorousPlantEntity;
import net.raptorzizi.fangs_n_claws.entity.skull.SkullEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.BabyWildWolfEntity;
import net.raptorzizi.fangs_n_claws.entity.hyena.HyenaEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.AcidCloudEntity;
import net.raptorzizi.fangs_n_claws.entity.skull.AcidSkullEntity;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.PotionsRegistry;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.raptorzizi.fangs_n_claws.effect.AcidEffect;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleWormArmEntity;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleWormEntity;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public class CommonSetup {

    private static final float OWL_FEATHER_PER_PIECE = 0.12f;
    private static final float OWL_FEATHER_MAX       = 0.8f;

    @SubscribeEvent
    public static void onOwlFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        int pieces = OwlArmorItem.countOwlPieces(player);
        if (pieces > 0) {
            float reduction = Math.min(pieces * OWL_FEATHER_PER_PIECE, OWL_FEATHER_MAX);
            event.setDamageMultiplier(event.getDamageMultiplier() * (1f - reduction));
        }
    }

    @SubscribeEvent
    public static void onOwlbearEggBaby(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof OwlbearEntity owlbear)) return;

        ItemStack stack = event.getItemStack();
        EntityType<? extends BabyOwlbearEntity> babyType;
        if (owlbear instanceof ShrikeEntity) {
            if (!stack.is(ItemsRegistry.SHRIKE_SPAWN_EGG.get())) return;
            babyType = EntityRegistry.BABY_SHRIKE.get();
        } else {
            if (!stack.is(ItemsRegistry.OWLBEAR_SPAWN_EGG.get())) return;
            babyType = EntityRegistry.BABY_OWLBEAR.get();
        }

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BabyOwlbearEntity baby = babyType.create(serverLevel);
            if (baby != null) {
                baby.moveTo(owlbear.getX(), owlbear.getY(), owlbear.getZ(), owlbear.getYRot(), 0f);
                baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()),
                        MobSpawnType.SPAWN_EGG, null);
                baby.setParent(owlbear);
                serverLevel.addFreshEntity(baby);
                if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);
            }
        }

        event.getEntity().swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    private static final float ACID_SPLASH_WIDTH    = 2.0f * 2.5f;
    private static final float ACID_SPLASH_HEIGHT   = 2.0f;
    private static final int   ACID_SPLASH_LIFETIME = 60;
    private static final float ACID_SPLASH_DAMAGE   = 3.0f;

    private static final float ACID_FOG_WIDTH    = 6.0f;
    private static final float ACID_FOG_HEIGHT   = 2.0f;
    private static final int   ACID_FOG_LIFETIME = 200;
    private static final float ACID_FOG_DAMAGE   = 1.0f;

    @SubscribeEvent
    public static void onBottleAcidSkull(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AcidSkullEntity skull)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.GLASS_BOTTLE)) return;
        if (skull.isHarvested()) return;

        Player player = event.getEntity();
        if (!event.getLevel().isClientSide) {
            skull.setHarvested(true);
            player.playSound(SoundEvents.BOTTLE_FILL_DRAGONBREATH, 1.0F, 1.0F);
            ItemStack filled = PotionContents.createItemStack(Items.POTION, PotionsRegistry.ACID);
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, filled));
        }

        player.swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    private static final double ACID_BOTTLE_RANGE = 2.0;

    @SubscribeEvent
    public static void onBottleAcidPuddleItem(PlayerInteractEvent.RightClickItem event) {
        if (tryBottleAcidPuddle(event.getEntity(), event.getHand(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }
    }

    @SubscribeEvent
    public static void onBottleAcidPuddleBlock(PlayerInteractEvent.RightClickBlock event) {
        if (tryBottleAcidPuddle(event.getEntity(), event.getHand(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }
    }

    private static boolean tryBottleAcidPuddle(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.is(Items.GLASS_BOTTLE)) return false;

        List<AcidCloudEntity> puddles = player.level().getEntitiesOfClass(AcidCloudEntity.class,
                player.getBoundingBox().inflate(ACID_BOTTLE_RANGE),
                cloud -> cloud.isAlive() && cloud.isAcidPool());
        if (puddles.isEmpty()) return false;

        if (!player.level().isClientSide) {
            puddles.get(0).discard();
            player.playSound(SoundEvents.BOTTLE_FILL_DRAGONBREATH, 1.0F, 1.0F);
            ItemStack filled = PotionContents.createItemStack(Items.POTION, PotionsRegistry.ACID);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
        }
        player.swing(hand);
        return true;
    }

    @SubscribeEvent
    public static void onAcidPotionImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!(potion.level() instanceof ServerLevel server)) return;

        ItemStack stack = potion.getItem();
        boolean splash    = stack.is(Items.SPLASH_POTION);
        boolean lingering = stack.is(Items.LINGERING_POTION);
        if (!splash && !lingering) return;

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return;
        boolean isAcid = false;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().equals(MobEffectsRegistry.ACID)) { isAcid = true; break; }
        }
        if (!isAcid) return;

        Vec3 hit = event.getRayTraceResult().getLocation();

        if (splash) {
            spawnAcidPuddle(server, hit);
            return;
        }

        event.setCanceled(true);
        server.levelEvent(2002, potion.blockPosition(), contents.getColor());
        spawnAcidFog(server, hit);
        potion.discard();
    }

    private static void spawnAcidFog(ServerLevel server, Vec3 pos) {
        AcidCloudEntity cloud = new AcidCloudEntity(EntityRegistry.ACID_CLOUD.get(), server);
        cloud.setPos(pos.x, pos.y, pos.z);
        cloud.setDimensions(ACID_FOG_WIDTH, ACID_FOG_HEIGHT);
        cloud.setParticlesEnabled(true);
        cloud.setAcidPool(false);
        cloud.setLifetime(ACID_FOG_LIFETIME);
        cloud.setTickDamage(ACID_FOG_DAMAGE);
        server.addFreshEntity(cloud);
    }

    private static void spawnAcidPuddle(ServerLevel server, Vec3 hit) {
        BlockHitResult ground = server.clip(new ClipContext(
                hit.add(0.0, 0.5, 0.0), hit.add(0.0, -4.0, 0.0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        double groundY = ground.getType() == HitResult.Type.BLOCK ? ground.getLocation().y : hit.y;

        AcidCloudEntity puddle = new AcidCloudEntity(EntityRegistry.ACID_CLOUD.get(), server);
        puddle.setPos(hit.x, groundY, hit.z);
        puddle.setDimensions(ACID_SPLASH_WIDTH, ACID_SPLASH_HEIGHT);
        puddle.setParticlesEnabled(false);
        puddle.setAcidPool(true);
        puddle.setLoopSound(true);
        puddle.setLifetime(ACID_SPLASH_LIFETIME);
        puddle.setTickDamage(ACID_SPLASH_DAMAGE);
        server.addFreshEntity(puddle);

        server.sendParticles(ParticlesRegistry.ACID_SPLASH.get(),
                hit.x, groundY + 0.05, hit.z, 0, 1.0, 0.0, 0.0, 1.0);
    }

    @SubscribeEvent
    public static void onWolfEggBaby(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof WildWolfEntity adult)) return;

        ItemStack stack = event.getItemStack();
        EntityType<? extends BabyWildWolfEntity> babyType;
        if (adult instanceof HyenaEntity) {
            if (!stack.is(ItemsRegistry.HYENA_SPAWN_EGG.get())) return;
            babyType = EntityRegistry.BABY_HYENA.get();
        } else {
            if (!stack.is(ItemsRegistry.WILD_WOLF_SPAWN_EGG.get())) return;
            babyType = EntityRegistry.BABY_WILD_WOLF.get();
        }

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BabyWildWolfEntity baby = babyType.create(serverLevel);
            if (baby != null) {
                baby.moveTo(adult.getX(), adult.getY(), adult.getZ(), adult.getYRot(), 0f);
                baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()),
                        MobSpawnType.SPAWN_EGG, null);
                baby.setVariant(adult.getVariant());
                baby.setParent(adult);
                serverLevel.addFreshEntity(baby);
                if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);
            }
        }

        event.getEntity().swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    @SubscribeEvent
    public static void onScorpionEggBaby(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof ScorpionEntity scorpion)) return;

        ItemStack stack = event.getItemStack();
        int variant = BabyScorpionEntity.variantOf(scorpion);
        var expectedEgg = switch (variant) {
            case BabyScorpionEntity.VARIANT_DESERT -> ItemsRegistry.DESERT_SCORPION_SPAWN_EGG.get();
            case BabyScorpionEntity.VARIANT_FROST  -> ItemsRegistry.FROST_SCORPION_SPAWN_EGG.get();
            case BabyScorpionEntity.VARIANT_NETHER -> ItemsRegistry.NETHER_SCORPION_SPAWN_EGG.get();
            default                                -> ItemsRegistry.SCORPION_SPAWN_EGG.get();
        };
        if (!stack.is(expectedEgg)) return;

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BabyScorpionEntity baby = EntityRegistry.BABY_SCORPION.get().create(serverLevel);
            if (baby != null) {
                baby.setVariant(variant);
                baby.moveTo(scorpion.getX(), scorpion.getY(), scorpion.getZ(), scorpion.getYRot(), 0f);
                baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()),
                        MobSpawnType.SPAWN_EGG, null);
                baby.setParent(scorpion);
                baby.setRideYaw(serverLevel.random.nextFloat() * 360f);
                serverLevel.addFreshEntity(baby);
                baby.startRiding(scorpion, false);
                if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);
            }
        }

        event.getEntity().swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    private static final int JUMPS_TO_BREAK_FREE = 4;
    private static final Map<UUID, Integer> frozenJumpCounts = new HashMap<>();
    private static final Map<UUID, Boolean> frozenPrevAI = new HashMap<>();

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.EVIL_BAT.get(),        EvilBatEntity.prepareAttributes().build());
        event.put(EntityRegistry.OGRE.get(),           OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.CAVE_OGRE.get(),      OgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREWOLF.get(),       WerewolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.OWLBEAR.get(),        OwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.BABY_OWLBEAR.get(),   BabyOwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.SHRIKE.get(),         OwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.BABY_SHRIKE.get(),    BabyOwlbearEntity.prepareAttributes().build());
        event.put(EntityRegistry.BABY_SCORPION.get(),  BabyScorpionEntity.prepareAttributes().build());
        event.put(EntityRegistry.SILVER_SKELETON.get(), SilverSkeletonEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOLEM.get(),           GolemEntity.prepareAttributes().build());
        event.put(EntityRegistry.ICE_GOLEM.get(),       GolemEntity.prepareAttributes().build());
        event.put(EntityRegistry.GHOST.get(),           GhostEntity.prepareAttributes().build());
        event.put(EntityRegistry.PURPLE_WORM.get(),     PurpleWormEntity.prepareAttributes().build());
        event.put(EntityRegistry.PURPLE_WORM_ARM.get(), PurpleWormArmEntity.prepareAttributes().build());
        event.put(EntityRegistry.MIMIC.get(),           MimicEntity.prepareAttributes().build());
        event.put(EntityRegistry.FIRE_GHOST.get(),      FireGhostEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOBLIN.get(),          GoblinEntity.prepareAttributes().build());
        event.put(EntityRegistry.DART_GOBLIN.get(),     DartGoblinEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREVILLAGER.get(),    WerevillagerEntity.prepareAttributes().build());
        event.put(EntityRegistry.IMP.get(),             ImpEntity.prepareAttributes().build());
        event.put(EntityRegistry.HELL_OGRE.get(),       HellOgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.HORSE_BAT.get(),          HorseBatEntity.prepareAttributes().build());
        event.put(EntityRegistry.NIGHTMARE_HORSE.get(),    NightmareHorseEntity.prepareAttributes().build());
        event.put(EntityRegistry.SKELETON_HORSE_MOB.get(), SkeletonHorseMob.prepareAttributes().build());
        event.put(EntityRegistry.ZOMBIE_HORSE_MOB.get(),   ZombieHorseMob.prepareAttributes().build());
        event.put(EntityRegistry.WILD_WOLF.get(),          WildWolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.HYENA.get(),              WildWolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.BABY_WILD_WOLF.get(),     BabyWildWolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.BABY_HYENA.get(),         BabyWildWolfEntity.prepareAttributes().build());
        event.put(EntityRegistry.CARNIVOROUS_PLANT.get(),  CarnivorousPlantEntity.prepareAttributes().build());
        event.put(EntityRegistry.CARNIVOROUS_PLANT_SPROUT.get(), CarnivorousPlantSproutEntity.prepareAttributes().build());
        event.put(EntityRegistry.FIRE_SKULL.get(),         SkullEntity.prepareAttributes().build());
        event.put(EntityRegistry.ACID_SKULL.get(),         SkullEntity.prepareAttributes().build());
        event.put(EntityRegistry.SCORPION.get(),          ScorpionEntity.prepareAttributes().build());
        event.put(EntityRegistry.DESERT_SCORPION.get(),  ScorpionEntity.prepareAttributes().build());
        event.put(EntityRegistry.FROST_SCORPION.get(),   ScorpionEntity.prepareAttributes().build());
        event.put(EntityRegistry.NETHER_SCORPION.get(),  ScorpionEntity.prepareAttributes().build());
    }

    @SubscribeEvent
    public static void onPitchforkThrown(LivingDamageEvent.Pre event) {
        if (event.getSource().getDirectEntity() instanceof FirePitchforkEntity pitchfork) {
            float diff = pitchfork.getBaseThrownDamage() - 8.0f;
            if (diff != 0) event.setNewDamage(event.getNewDamage() + diff);
        }
    }

    @SubscribeEvent
    public static void onCatchingClawMelee(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getDirectEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (weapon.is(ItemsRegistry.CATCHING_CLAW.get()) || weapon.is(ItemsRegistry.CATCHING_CLAW_NETHERITE.get())) {
            event.setNewDamage(1.0f);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity.hasEffect(MobEffectsRegistry.STUNNED)) entity.removeEffect(MobEffectsRegistry.STUNNED);
        if (entity.hasEffect(MobEffectsRegistry.FROZEN)) {
            entity.removeEffect(MobEffectsRegistry.FROZEN); // déclenche onFrozenRemoved
            frozenJumpCounts.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onFrozenEffectAdded(MobEffectEvent.Added event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || !(instance.getEffect().value() instanceof FrozenEffect)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (entity instanceof Mob mob) {
            frozenPrevAI.put(entity.getUUID(), !mob.isNoAi());
            mob.setNoAi(true);
        }

        entity.setTicksFrozen(0);

        UUID entityUUID = entity.getUUID();
        boolean alreadyExists = entity.level()
                .getEntitiesOfClass(FrozenBoxEntity.class, entity.getBoundingBox().inflate(2))
                .stream()
                .anyMatch(box -> entityUUID.equals(box.getTargetUUID()));
        if (alreadyExists) return;

        FrozenBoxEntity box = new FrozenBoxEntity(EntityRegistry.FROZEN_BOX.get(), entity.level());
        box.setTarget(entity);
        box.setPos(entity.getX(), entity.getY(), entity.getZ());
        entity.level().addFreshEntity(box);
    }

    @SubscribeEvent
    public static void onFrozenEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEffect().value() instanceof FrozenEffect)) return;
        onUnfreeze(event.getEntity());
    }

    @SubscribeEvent
    public static void onFrozenEffectExpired(MobEffectEvent.Expired event) {
        // getEffectInstance() est @Nullable chez NeoForge (le champ de MobEffectEvent l'est) :
        // le crash 1.2.4 « Ticking player » venait de ce dereferencement non garde.
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || !(instance.getEffect().value() instanceof FrozenEffect)) return;
        onUnfreeze(event.getEntity());
    }

    private static void onUnfreeze(LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        if (entity instanceof Mob mob) {
            Boolean prevAI = frozenPrevAI.remove(entity.getUUID());
            if (Boolean.TRUE.equals(prevAI) && mob.isNoAi()) {
                mob.setNoAi(false);
            }
        }
        frozenJumpCounts.remove(entity.getUUID());

        if (entity.level() instanceof ServerLevel serverLevel) {
            double cx = entity.getX();
            double cy = entity.getY() + entity.getBbHeight() * 0.5;
            double cz = entity.getZ();
            double rx = entity.getBbWidth() * 0.5;
            double ry = entity.getBbHeight() * 0.4;

            serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                cx, cy, cz, 40, rx, ry, rx, 0.25
            );
            serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                cx, cy, cz, 30, rx, ry, rx, 0.3
            );
            serverLevel.playSound(null, cx, entity.getY(), cz,
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS,
                1.2f, 0.7f + serverLevel.getRandom().nextFloat() * 0.3f
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onIceGolemAttack(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity.hasEffect(MobEffectsRegistry.FROZEN)) return;

        Entity direct   = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();

        boolean isIceGolemMelee      = direct instanceof IceGolemEntity;
        boolean isIceGolemProjectile = direct instanceof BlockProjectile && attacker instanceof IceGolemEntity;
        if (!isIceGolemMelee && !isIceGolemProjectile) return;

        int prevTicks = entity.getTicksFrozen();
        int threshold = entity.getTicksRequiredToFreeze();

        entity.setTicksFrozen(threshold - 20);

        if (prevTicks > 0) {
            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.FROZEN, 100, 0, false, false));
            entity.setTicksFrozen(0);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;
        if (entity.hasEffect(MobEffectsRegistry.FROZEN)) return;

        if (entity.getTicksFrozen() >= entity.getTicksRequiredToFreeze()) {
            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.FROZEN, 100, 0, false, false));
            entity.setTicksFrozen(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTotemOfFrost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (player.getHealth() > player.getMaxHealth() * 0.25f) return;

        ItemStack totem = ItemStack.EMPTY;
        if (player.getOffhandItem().is(ItemsRegistry.TOTEM_OF_FROST.get())) {
            totem = player.getOffhandItem();
        } else if (player.getMainHandItem().is(ItemsRegistry.TOTEM_OF_FROST.get())) {
            totem = player.getMainHandItem();
        }
        if (totem.isEmpty()) return;

        player.setHealth(player.getMaxHealth());
        player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(10),
                e -> e != player && e.isAlive())
            .forEach(e -> e.addEffect(new MobEffectInstance(MobEffectsRegistry.FROZEN, 100, 0, false, false)));

        ItemStack displayItem = totem.copy();
        totem.shrink(1);

        if (player instanceof ServerPlayer frostPlayer) {
            FncAdvancements.grant(frostPlayer, "forge/frozen_savior");
        }
        if (player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new TotemFrostPayload(displayItem));

            double cx = player.getX(), cy = player.getY() + player.getBbHeight() * 0.5, cz = player.getZ();
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                cx, cy, cz, 120, 3.0, 1.5, 3.0, 0.3);
            serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                cx, cy, cz, 80, 2.5, 1.2, 2.5, 0.2);
            serverLevel.playSound(null, cx, player.getY(), cz,
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            serverLevel.playSound(null, cx, player.getY(), cz,
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.5f, 0.5f);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Player)) return;

        if (!entity.hasEffect(MobEffectsRegistry.FROZEN)) {
            frozenJumpCounts.remove(entity.getUUID());
            return;
        }

        UUID uuid  = entity.getUUID();
        int  count = frozenJumpCounts.getOrDefault(uuid, 0) + 1;

        if (count >= JUMPS_TO_BREAK_FREE) {
            entity.removeEffect(MobEffectsRegistry.FROZEN);
            frozenJumpCounts.remove(uuid);
        } else {
            frozenJumpCounts.put(uuid, count);
        }
    }

    /**
     * Acide : double l'usure de chaque piece d'armure quand son porteur encaisse des degats.
     * La reduction d'armure/resistance, elle, passe par les modificateurs d'attributs de l'effet.
     */
    @SubscribeEvent
    public static void onArmorHurt(ArmorHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!entity.hasEffect(MobEffectsRegistry.ACID)) return;

        for (EquipmentSlot slot
                : EquipmentSlot.values()) {
            float damage = event.getNewDamage(slot);
            if (damage > 0.0f) {
                event.setNewDamage(slot,
                        damage * AcidEffect.DURABILITY_MULTIPLIER);
            }
        }
    }
}