package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class NightmareVehicleHealthShift {

    private static final int SHIFT = 8;
    private static boolean shifted = false;

    @SubscribeEvent
    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.VEHICLE_HEALTH.equals(event.getName())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof NightmareHorseEntity)) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0.0F, -SHIFT, 0.0F);
        shifted = true;
    }

    @SubscribeEvent
    public static void onPost(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.VEHICLE_HEALTH.equals(event.getName())) return;
        if (!shifted) return;

        event.getGuiGraphics().pose().popPose();
        shifted = false;
    }
}
