package net.raptorzizi.fangs_n_claws.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AcidArmorHudOverlay {

    private static final int ICON = 9;
    private static final int V = 9, U_EMPTY = 16, U_HALF = 25, U_FULL = 34;

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    private static final ResourceLocation ACID_ARMOR_FULL =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/gui/sprites/hud/acid_armor_full.png");
    private static final ResourceLocation ACID_ARMOR_HALF =
            new ResourceLocation(FangsClawsMod.MOD_ID, "textures/gui/sprites/hud/acid_armor_half.png");

    @SubscribeEvent
    public static void onRenderArmorLayer(RenderGuiOverlayEvent.Pre event) {
        if (!VanillaGuiOverlay.ARMOR_LEVEL.type().equals(event.getOverlay())) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.hasEffect(MobEffectsRegistry.ACID.get())) return;

        event.setCanceled(true);

        int armor = player.getArmorValue();
        if (armor <= 0) return;
        int beforeAcid = armor * 2;

        GuiGraphics gui = event.getGuiGraphics();
        ForgeGui forgeGui = (ForgeGui) mc.gui;
        int x = gui.guiWidth() / 2 - 91;
        int y = gui.guiHeight() - forgeGui.leftHeight;

        RenderSystem.enableBlend();
        for (int i = 0; i < 10; i++) {
            int px = x + i * 8;
            int point = i * 2 + 1;
            if (point < armor) {
                gui.blit(ICONS, px, y, U_FULL, V, ICON, ICON);
            } else if (point == armor) {
                gui.blit(ICONS, px, y, U_HALF, V, ICON, ICON);
            } else if (point < beforeAcid) {
                gui.blit(ACID_ARMOR_FULL, px, y, ICON, ICON, 0.0F, 0.0F, ICON, ICON, ICON, ICON);
            } else if (point == beforeAcid) {
                gui.blit(ACID_ARMOR_HALF, px, y, ICON, ICON, 0.0F, 0.0F, ICON, ICON, ICON, ICON);
            } else {
                gui.blit(ICONS, px, y, U_EMPTY, V, ICON, ICON);
            }
        }
        RenderSystem.disableBlend();

        forgeGui.leftHeight += 10;
    }
}
