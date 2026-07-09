package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class NightmareStaminaLayer implements IGuiOverlay {

    private static final int BAR_WIDTH  = 182;
    private static final int BAR_HEIGHT = 3;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof NightmareHorseEntity horse)) return;

        float stamina = horse.getStamina();
        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = screenHeight - 31 + BAR_HEIGHT;

        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF40200C);

        int filled = Math.round(stamina * BAR_WIDTH);
        if (filled > 0) {
            int color = stamina < 0.3F ? 0xFFFF3300 : 0xFFFF8A00;
            guiGraphics.fill(x, y, x + filled, y + BAR_HEIGHT, color);
        }
    }
}
