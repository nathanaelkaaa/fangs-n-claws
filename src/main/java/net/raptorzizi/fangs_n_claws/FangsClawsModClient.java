package net.raptorzizi.fangs_n_claws;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

import java.util.List;


@Mod(value = FangsClawsMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class FangsClawsModClient {
    public FangsClawsModClient(ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ItemsRegistry.FANG_DAGGER.get(),
                FangsClawsMod.id("backstab"),
                (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player) || level == null) return 0f;
                    if (player.getCooldowns().isOnCooldown(stack.getItem())) return 0f;
                    AABB box = player.getBoundingBox().inflate(4.0);
                    List<LivingEntity> nearby = level.getEntitiesOfClass(
                            LivingEntity.class, box, e -> e != player && e.isAlive());
                    for (LivingEntity target : nearby) {
                        if (FangDaggerItem.isBackstab(player, target)) return 1f;
                    }
                    return 0f;
                });

            ItemProperties.register(ItemsRegistry.CATCHING_CLAW.get(),
                FangsClawsMod.id("catching_claw_cast"),
                (stack, level, entity, seed) -> {
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data == null) return 0f;
                    return data.copyTag().contains("HookUUID") ? 1f : 0f;
                });
        });
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.OGRE.get(), OgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CATCHING_CLAW_HOOK.get(), CatchingClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EVIL_EYE_PROJECTILE.get(), ThrownItemRenderer::new);
    }
}