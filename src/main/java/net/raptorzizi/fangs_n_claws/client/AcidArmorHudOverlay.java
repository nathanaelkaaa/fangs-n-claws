package net.raptorzizi.fangs_n_claws.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class AcidArmorHudOverlay {

    private static final ResourceLocation ARMOR_EMPTY =
            ResourceLocation.withDefaultNamespace("hud/armor_empty");
    private static final ResourceLocation ARMOR_HALF =
            ResourceLocation.withDefaultNamespace("hud/armor_half");
    private static final ResourceLocation ARMOR_FULL =
            ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation ACID_ARMOR_HALF =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "hud/acid_armor_half");
    private static final ResourceLocation ACID_ARMOR_FULL =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "hud/acid_armor_full");

    @SubscribeEvent
    public static void onRenderArmorLayer(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.ARMOR_LEVEL.equals(event.getName())) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.hasEffect(MobEffectsRegistry.ACID)) return;

        event.setCanceled(true);

        GuiGraphics gui = event.getGuiGraphics();
        int armor = player.getArmorValue();
        if (armor <= 0) return;
        int beforeAcid = armor * 2;

        int x = gui.guiWidth() / 2 - 91;
        int y = gui.guiHeight() - mc.gui.leftHeight;

        RenderSystem.enableBlend();
        for (int i = 0; i < 10; i++) {
            int px = x + i * 8;
            int point = i * 2 + 1;
            if (point < armor) {
                gui.blitSprite(ARMOR_FULL, px, y, 9, 9);
            } else if (point == armor) {
                gui.blitSprite(ARMOR_HALF, px, y, 9, 9);
            } else if (point < beforeAcid) {
                gui.blitSprite(ACID_ARMOR_FULL, px, y, 9, 9);
            } else if (point == beforeAcid) {
                gui.blitSprite(ACID_ARMOR_HALF, px, y, 9, 9);
            } else {
                gui.blitSprite(ARMOR_EMPTY, px, y, 9, 9);
            }
        }
        RenderSystem.disableBlend();

        mc.gui.leftHeight += 10;
    }
}
