package net.raptorzizi.fangs_n_claws.setup;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.raptorzizi.fangs_n_claws.entity.evil_bat.EvilBatRenderer;
import net.raptorzizi.fangs_n_claws.entity.ghost.GhostRenderer;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.DartGoblinRenderer;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartRenderer;
import net.raptorzizi.fangs_n_claws.entity.goblin.GoblinRenderer;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemRenderer;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.projectile.BlockProjectileRenderer;
import net.raptorzizi.fangs_n_claws.entity.cave_ogre.CaveOgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.velocity_arrow.VelocityArrowRenderer;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearRenderer;
import net.raptorzizi.fangs_n_claws.entity.silver_skeleton.SilverSkeletonRenderer;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfRenderer;
import net.raptorzizi.fangs_n_claws.entity.werevillager.WerevillagerRenderer;
import net.raptorzizi.fangs_n_claws.block.GhostBlockRenderer;
import net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork.DecrepitPitchforkRenderer;
import net.raptorzizi.fangs_n_claws.entity.hell_ogre.HellOgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpRenderer;
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

@EventBusSubscriber(
        modid = FangsClawsMod.MOD_ID,
        bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.EVIL_BAT.get(),         EvilBatRenderer::new);
        event.registerEntityRenderer(EntityRegistry.OGRE.get(),            OgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CAVE_OGRE.get(),       CaveOgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HELL_OGRE.get(),       HellOgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEREWOLF.get(),        WerewolfRenderer::new);
        event.registerEntityRenderer(EntityRegistry.OWLBEAR.get(),         OwlbearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SILVER_SKELETON.get(), SilverSkeletonRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLEM.get(),               GolemRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GHOST.get(),               GhostRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOBLIN.get(),              GoblinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DART_GOBLIN.get(),              DartGoblinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POISONOUS_DART.get(),          PoisonousDartRenderer::new);
        event.registerEntityRenderer(EntityRegistry.IMP.get(),                      ImpRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DECREPIT_PITCHFORK_ENTITY.get(), DecrepitPitchforkRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEREVILLAGER.get(),        WerevillagerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CATCHING_CLAW_HOOK.get(),     CatchingClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.NETHERITE_CLAW_HOOK.get(),   NetheriteClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOCK_PROJECTILE.get(),      BlockProjectileRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VELOCITY_ARROW_ENTITY.get(), VelocityArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EVIL_EYE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.GHOST_BLOCK_ENTITY.get(), GhostBlockRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticlesRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.BLOOD_GROUND.get(),   BloodGroundParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.STUN_STAR.get(),      StunStarParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.BLACK_FOG.get(),          BlackFogParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.FIRE_EXPLOSION.get(),   FireExplosionParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.FIRE.get(),            FireParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.ORANGE_SMOKE.get(),    OrangeSmokeParticle.Provider::new);
    }
}