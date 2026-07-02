package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseArmorScreen;
import net.raptorzizi.fangs_n_claws.registries.MenuTypeRegistry;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareStaminaLayer;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.entity.frozen_box.FrozenBoxRenderer;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatRenderer;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostRenderer;
import net.raptorzizi.fangs_n_claws.entity.fire_ghost.FireGhostRenderer;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinRenderer;
import net.raptorzizi.fangs_n_claws.entity.projectile.PoisonousDartRenderer;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinRenderer;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemRenderer;
import net.raptorzizi.fangs_n_claws.entity.ice_golem.IceGolemRenderer;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectileRenderer;
import net.raptorzizi.fangs_n_claws.entity.velocity_arrow.VelocityArrowRenderer;
import net.raptorzizi.fangs_n_claws.entity.projectile.FeatherProjectileRenderer;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearRenderer;
import net.raptorzizi.fangs_n_claws.entity.owlbear.BabyOwlbearRenderer;
import net.raptorzizi.fangs_n_claws.entity.scorpion.BabyScorpionRenderer;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMobRenderer;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfRenderer;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionRenderer;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonRenderer;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfRenderer;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerRenderer;
import net.raptorzizi.fangs_n_claws.block.GhostBlockRenderer;
import net.raptorzizi.fangs_n_claws.particle.BlackFogParticle;
import net.raptorzizi.fangs_n_claws.particle.BloodGroundParticle;
import net.raptorzizi.fangs_n_claws.particle.BloodParticle;
import net.raptorzizi.fangs_n_claws.particle.FireExplosionParticle;
import net.raptorzizi.fangs_n_claws.particle.FireParticle;
import net.raptorzizi.fangs_n_claws.particle.OrangeSmokeParticle;
import net.raptorzizi.fangs_n_claws.particle.StunStarParticle;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = FangsClawsMod.MOD_ID,
        //bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    private static final Map<Integer, float[]> frozenYaws     = new HashMap<>();
    private static final Map<Integer, float[]> savedRotations = new HashMap<>();

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuTypeRegistry.HORSE_ARMOR.get(), HorseArmorScreen::new);
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(MobEffectsRegistry.FROZEN)) {
            frozenYaws.remove(entity.getId());
            return;
        }

        entity.walkAnimation.setSpeed(0);
        entity.walkAnimation.update(0, 0);
        entity.attackAnim  = 0f;
        entity.oAttackAnim = 0f;

        int id = entity.getId();
        frozenYaws.computeIfAbsent(id, k -> new float[]{ entity.yHeadRot, entity.yBodyRot });
        float[] yaws = frozenYaws.get(id);

        savedRotations.put(id, new float[]{ entity.yHeadRot, entity.yHeadRotO, entity.yBodyRot, entity.yBodyRotO });
        entity.yHeadRot  = entity.yHeadRotO  = yaws[0];
        entity.yBodyRot  = entity.yBodyRotO  = yaws[1];
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        float[] saved = savedRotations.remove(event.getEntity().getId());
        if (saved != null) {
            LivingEntity entity  = event.getEntity();
            entity.yHeadRot  = saved[0];
            entity.yHeadRotO = saved[1];
            entity.yBodyRot  = saved[2];
            entity.yBodyRotO = saved[3];
        }
    }

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.HORSE_BAT.get(),         HorseMobRenderer::new);
        event.registerEntityRenderer(EntityRegistry.NIGHTMARE_HORSE.get(),   HorseMobRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SKELETON_HORSE_MOB.get(), HorseMobRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ZOMBIE_HORSE_MOB.get(),   HorseMobRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WILD_WOLF.get(),         WildWolfRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EVIL_BAT.get(),         EvilBatRenderer::new);
        event.registerEntityRenderer(EntityRegistry.OGRE.get(),            OgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CAVE_OGRE.get(),       CaveOgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEREWOLF.get(),        WerewolfRenderer::new);
        event.registerEntityRenderer(EntityRegistry.OWLBEAR.get(),         OwlbearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BABY_OWLBEAR.get(),    BabyOwlbearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHRIKE.get(),          OwlbearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BABY_SHRIKE.get(),     BabyOwlbearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SILVER_SKELETON.get(), SilverSkeletonRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SCORPION.get(),         ScorpionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DESERT_SCORPION.get(),  ScorpionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FROST_SCORPION.get(),   ScorpionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.NETHER_SCORPION.get(),  ScorpionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BABY_SCORPION.get(),     BabyScorpionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FROZEN_BOX.get(),       FrozenBoxRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLEM.get(),               GolemRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_GOLEM.get(),           IceGolemRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GHOST.get(),               GhostRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_GHOST.get(),          FireGhostRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOBLIN.get(),              GoblinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DART_GOBLIN.get(),         DartGoblinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISONOUS_DART.get(),      PoisonousDartRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEREVILLAGER.get(),        WerevillagerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CATCHING_CLAW_HOOK.get(),     CatchingClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.NETHERITE_CLAW_HOOK.get(),   NetheriteClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOCK_PROJECTILE.get(),      BlockProjectileRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VELOCITY_ARROW_ENTITY.get(), VelocityArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FEATHER_PROJECTILE.get(),    FeatherProjectileRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.GHOST_BLOCK_ENTITY.get(), GhostBlockRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "nightmare_stamina"),
                new NightmareStaminaLayer());
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticlesRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.BLOOD_GROUND.get(),   BloodGroundParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.STUN_STAR.get(),      StunStarParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.BLACK_FOG.get(),        BlackFogParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.FIRE_EXPLOSION.get(),  FireExplosionParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.FIRE.get(),            FireParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.ORANGE_SMOKE.get(),    OrangeSmokeParticle.Provider::new);
    }
}
