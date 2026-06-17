package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.raptorzizi.fangs_n_claws.network.TotemFrostPayload;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.raptorzizi.fangs_n_claws.effect.FrozenEffect;
import net.raptorzizi.fangs_n_claws.entity.frozen_box.FrozenBoxEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.Mob;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemEntity;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatEntity;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostEntity;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinEntity;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpEntity;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonEntity;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreEntity;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
// bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {

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
        event.put(EntityRegistry.SILVER_SKELETON.get(), SilverSkeletonEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOLEM.get(),           GolemEntity.prepareAttributes().build());
        event.put(EntityRegistry.ICE_GOLEM.get(),       GolemEntity.prepareAttributes().build());
        event.put(EntityRegistry.GHOST.get(),           GhostEntity.prepareAttributes().build());
        event.put(EntityRegistry.GOBLIN.get(),          GoblinEntity.prepareAttributes().build());
        event.put(EntityRegistry.DART_GOBLIN.get(),     DartGoblinEntity.prepareAttributes().build());
        event.put(EntityRegistry.WEREVILLAGER.get(),    WerevillagerEntity.prepareAttributes().build());
        event.put(EntityRegistry.IMP.get(),             ImpEntity.prepareAttributes().build());
        event.put(EntityRegistry.HELL_OGRE.get(),       HellOgreEntity.prepareAttributes().build());
        event.put(EntityRegistry.SCORPION.get(),         ScorpionEntity.prepareAttributes().build());
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
        if (!(event.getEffectInstance().getEffect().value() instanceof FrozenEffect)) return;
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
        if (!(event.getEffectInstance().getEffect().value() instanceof FrozenEffect)) return;
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
}