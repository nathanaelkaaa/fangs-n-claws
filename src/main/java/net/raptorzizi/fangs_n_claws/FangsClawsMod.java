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
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.EffectParticleModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.raptorzizi.fangs_n_claws.config.ClientConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;
import net.raptorzizi.fangs_n_claws.effect.BleedingEffect;
import net.raptorzizi.fangs_n_claws.registries.CreativeModeTabs;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
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
        SoundsRegistry.register(modEventBus);
        ParticlesRegistry.register(modEventBus);
        MobEffectsRegistry.register(modEventBus);
        CreativeModeTabs.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", FangsClawsMod.MOD_ID));
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", FangsClawsMod.MOD_ID));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
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
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, path);
    }

}
