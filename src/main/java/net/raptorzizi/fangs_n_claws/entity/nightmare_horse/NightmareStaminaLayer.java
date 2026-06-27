package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public class NightmareStaminaLayer implements LayeredDraw.Layer {

    private static final int BAR_WIDTH  = 182;
    private static final int BAR_HEIGHT = 3;

    @Override
    public void render(GuiGraphics gui, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof NightmareHorseEntity horse)) return;

        float stamina = horse.getStamina();
        int x = (gui.guiWidth() - BAR_WIDTH) / 2;
        int y = gui.guiHeight() - 31 + BAR_HEIGHT;

        gui.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        gui.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF40200C);

        int filled = Math.round(stamina * BAR_WIDTH);
        if (filled > 0) {
            int color = stamina < 0.3F ? 0xFFFF3300 : 0xFFFF8A00;
            gui.fill(x, y, x + filled, y + BAR_HEIGHT, color);
        }
    }
}
