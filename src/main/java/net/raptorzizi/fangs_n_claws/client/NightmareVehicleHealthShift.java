package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity;

/**
 * Quand on chevauche un Nightmare Horse, la barre de stamina custom prend la place de la barre de
 * saut : on remonte les coeurs du joueur ET la barre de vie de la monture pour eviter le chevauchement.
 */
@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NightmareVehicleHealthShift {

    private static final int SHIFT = 8;
    private static boolean shifted = false;

    private static boolean isShiftable(Object overlay) {
        return VanillaGuiOverlay.PLAYER_HEALTH.type().equals(overlay)
                || VanillaGuiOverlay.MOUNT_HEALTH.type().equals(overlay);
    }

    private static boolean onNightmare() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getVehicle() instanceof NightmareHorseEntity;
    }

    @SubscribeEvent
    public static void onPre(RenderGuiOverlayEvent.Pre event) {
        if (!isShiftable(event.getOverlay()) || !onNightmare()) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0.0F, -SHIFT, 0.0F);
        shifted = true;
    }

    @SubscribeEvent
    public static void onPost(RenderGuiOverlayEvent.Post event) {
        if (!isShiftable(event.getOverlay()) || !shifted) return;

        event.getGuiGraphics().pose().popPose();
        shifted = false;
    }
}
