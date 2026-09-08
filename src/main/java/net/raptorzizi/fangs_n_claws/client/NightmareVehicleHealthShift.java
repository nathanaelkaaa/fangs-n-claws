package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class NightmareVehicleHealthShift {

    private static final int SHIFT = 8;

    private static final Set<ResourceLocation> SHIFTED_LAYERS =
            Set.of(VanillaGuiLayers.VEHICLE_HEALTH, VanillaGuiLayers.PLAYER_HEALTH);

    private static final Set<ResourceLocation> PUSHED = new HashSet<>();

    private static boolean isShiftedLayer(ResourceLocation layer) {
        return SHIFTED_LAYERS.contains(layer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (!isShiftedLayer(event.getName())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof NightmareHorseEntity)) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0.0F, -SHIFT, 0.0F);
        PUSHED.add(event.getName());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPost(RenderGuiLayerEvent.Post event) {
        if (!PUSHED.remove(event.getName())) return;

        event.getGuiGraphics().pose().popPose();
    }
}
