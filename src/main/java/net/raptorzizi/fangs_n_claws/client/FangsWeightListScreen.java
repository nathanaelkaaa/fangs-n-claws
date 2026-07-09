package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Spawn-weight sub-page (grid of small numeric fields with an inline label).
 * ServerConfigs is a SERVER config, so its values are only loaded while in a
 * world; opened from the main menu the fields fall back to defaults and are
 * read-only (with a notice).
 */
public class FangsWeightListScreen extends Screen {

    public record Entry(String label, ForgeConfigSpec.IntValue cfg, int def) {}

    private final Screen parent;
    private final List<Entry> entries;
    private EditBox[] boxes;
    private boolean available = true;

    private static final int COLS    = 3;
    private static final int COL_W   = 122;
    private static final int TOP_Y   = 40;
    private static final int LABEL_W = 80;
    private static final int BOX_W   = 34;
    private static final int BOX_H   = 14;

    private int rowH = 22;

    public FangsWeightListScreen(Screen parent, Component title, List<Entry> entries) {
        super(title);
        this.parent  = parent;
        this.entries = entries;
    }

    @Override
    protected void init() {
        super.init();

        // SERVER config is only loaded inside a world — probe availability once.
        try {
            if (!entries.isEmpty()) entries.get(0).cfg().get();
            available = true;
        } catch (Exception e) {
            available = false;
        }

        boxes = new EditBox[entries.size()];
        int leftX = this.width / 2 - (COLS * COL_W) / 2;
        int rows  = (int) Math.ceil((double) entries.size() / COLS);
        this.rowH = Math.min(22, Math.max(16, (this.height - 34 - TOP_Y) / Math.max(rows, 1)));

        for (int i = 0; i < entries.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int cellX = leftX + col * COL_W;
            int cellY = TOP_Y + row * rowH;
            int val   = available ? entries.get(i).cfg().get() : entries.get(i).def();

            EditBox box = new EditBox(this.font, cellX + LABEL_W, cellY, BOX_W, BOX_H,
                    Component.literal(entries.get(i).label()));
            box.setValue(String.valueOf(val));
            box.setMaxLength(3);
            box.setFilter(s -> s.isEmpty() || s.matches("\\d{0,3}"));
            box.setEditable(available);
            this.addRenderableWidget(box);
            boxes[i] = box;
        }

        int cx = this.width / 2;
        int bottomY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Save & Back"),
                b -> { save(); this.minecraft.setScreen(parent); }).bounds(cx - 105, bottomY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"),
                b -> this.minecraft.setScreen(parent)).bounds(cx + 5, bottomY, 100, 20).build());
    }

    private void save() {
        if (!available) return;
        for (int i = 0; i < entries.size(); i++) {
            ForgeConfigSpec.IntValue cfg = entries.get(i).cfg();
            cfg.set(parse(boxes[i], cfg.get()));
        }
    }

    private static int parse(EditBox box, int fallback) {
        try {
            return Math.max(0, Math.min(500, Integer.parseInt(box.getValue())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        if (!available) {
            graphics.drawCenteredString(this.font,
                    Component.literal("§7Join a world to edit spawn rates"), this.width / 2, 26, 0xAAAAAA);
        }

        int leftX = this.width / 2 - (COLS * COL_W) / 2;
        for (int i = 0; i < entries.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int lx = leftX + col * COL_W;
            int ly = TOP_Y + row * rowH + 3;
            graphics.drawString(this.font, entries.get(i).label(), lx, ly, 0xCCCCCC, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
