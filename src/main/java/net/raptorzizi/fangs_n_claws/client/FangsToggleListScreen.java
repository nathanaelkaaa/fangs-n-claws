package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Generic boolean-toggle sub-page (grid of ON/OFF buttons).
 * Data-driven: each entry pairs a display name with its config value, so pages
 * can be reordered or extended without any index bookkeeping. Used for both the
 * "Spawn Toggles" and "Behavior" categories.
 */
public class FangsToggleListScreen extends Screen {

    public record Entry(String name, ForgeConfigSpec.BooleanValue cfg, boolean def) {}

    private final Screen parent;
    private final List<Entry> entries;
    private final boolean[] values;
    private Button[] buttons;

    private static final int COLS  = 3;
    private static final int COL_W = 112;
    private static final int TOP_Y = 36;

    public FangsToggleListScreen(Screen parent, Component title, List<Entry> entries) {
        super(title);
        this.parent  = parent;
        this.entries = entries;
        this.values  = new boolean[entries.size()];
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < entries.size(); i++) values[i] = entries.get(i).cfg().get();
        buttons = new Button[entries.size()];

        int leftX = this.width / 2 - (COLS * COL_W) / 2;
        int rows  = (int) Math.ceil((double) entries.size() / COLS);
        int rowH  = Math.min(24, Math.max(14, (this.height - 34 - TOP_Y) / Math.max(rows, 1)));
        int btnH  = Math.min(18, rowH - 3);

        for (int i = 0; i < entries.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int bx  = leftX + col * COL_W;
            int by  = TOP_Y + row * rowH;
            int idx = i;
            buttons[i] = Button.builder(label(i), b -> {
                values[idx] = !values[idx];
                b.setMessage(label(idx));
            }).bounds(bx, by, COL_W - 4, btnH).build();
            this.addRenderableWidget(buttons[i]);
        }

        int cx = this.width / 2;
        int bottomY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Save & Back"),
                b -> { save(); this.minecraft.setScreen(parent); }).bounds(cx - 105, bottomY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"),
                b -> this.minecraft.setScreen(parent)).bounds(cx + 5, bottomY, 100, 20).build());
    }

    private Component label(int i) {
        return Component.literal(entries.get(i).name() + ": " + (values[i] ? "§aON" : "§cOFF"));
    }

    private void save() {
        for (int i = 0; i < entries.size(); i++) entries.get(i).cfg().set(values[i]);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
