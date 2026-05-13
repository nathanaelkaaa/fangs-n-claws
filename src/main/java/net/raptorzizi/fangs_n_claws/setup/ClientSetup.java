package net.raptorzizi.fangs_n_claws.setup;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearRenderer;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfRenderer;
import net.raptorzizi.fangs_n_claws.particle.BloodGroundParticle;
import net.raptorzizi.fangs_n_claws.particle.BloodParticle;
import net.raptorzizi.fangs_n_claws.particle.StunStarParticle;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

@EventBusSubscriber(
        modid = FangsClawsMod.MOD_ID,
        //bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.OGRE.get(),     OgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEREWOLF.get(), WerewolfRenderer::new);
        event.registerEntityRenderer(EntityRegistry.OWLBEAR.get(),  OwlbearRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticlesRegistry.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.BLOOD_GROUND.get(),   BloodGroundParticle.Provider::new);
        event.registerSpriteSet(ParticlesRegistry.STUN_STAR.get(),        StunStarParticle.Provider::new);
    }
}