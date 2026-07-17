package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Generic boolean-toggle sub-page (grid of ON/OFF buttons), with optional
 * numeric fields rendered underneath. Data-driven: each entry pairs a display
 * name with its config value, so pages can be reordered or extended without any
 * index bookkeeping. Used for the "Spawn Toggles", "Behavior" (which also carries
 * the Mimic spawn-chance field) and "Spawn Limits" (per-dimension caps) categories.
 */
public class FangsToggleListScreen extends Screen {

    public record Entry(String name, ForgeConfigSpec.BooleanValue cfg, boolean def) {}

    /** A whole-number field (e.g. Mimic spawn chance, dimension cap), clamped to [min, max]. */
    public record IntEntry(String name, ForgeConfigSpec.IntValue cfg, int def, int min, int max) {}

    private final Screen parent;
    private final String subtitle;
    private final List<Entry> entries;
    private final List<IntEntry> intEntries;
    private final boolean[] values;
    private Button[] buttons;
    private EditBox[] intBoxes;

    private static final int COLS  = 3;
    private static final int COL_W = 112;
    private static final int TOP_Y = 36;

    public FangsToggleListScreen(Screen parent, Component title, List<Entry> entries) {
        this(parent, title, null, entries, List.of());
    }

    public FangsToggleListScreen(Screen parent, Component title, List<Entry> entries, List<IntEntry> intEntries) {
        this(parent, title, null, entries, intEntries);
    }

    public FangsToggleListScreen(Screen parent, Component title, String subtitle,
                                 List<Entry> entries, List<IntEntry> intEntries) {
        super(title);
        this.parent     = parent;
        this.subtitle   = subtitle;
        this.entries    = entries;
        this.intEntries = intEntries;
        this.values     = new boolean[entries.size()];
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

        // Numeric fields, laid out one per row below the toggle grid.
        intBoxes = new EditBox[intEntries.size()];
        int intY = TOP_Y + (entries.isEmpty() ? 4 : rows * rowH + 8);
        for (int i = 0; i < intEntries.size(); i++) {
            IntEntry e = intEntries.get(i);
            int boxW = 60;
            int boxX = this.width / 2 + 20;
            int boxY = intY + i * 22;
            EditBox box = new EditBox(this.font, boxX, boxY, boxW, 16, Component.literal(e.name()));
            box.setValue(String.valueOf(e.cfg().get()));
            box.setMaxLength(7);
            box.setFilter(s -> s.isEmpty() || s.matches("-?\\d{0,6}"));
            this.addRenderableWidget(box);
            intBoxes[i] = box;
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
        for (int i = 0; i < intEntries.size(); i++) {
            IntEntry e = intEntries.get(i);
            e.cfg().set(parse(intBoxes[i], e.cfg().get(), e.min(), e.max()));
        }
    }

    private static int parse(EditBox box, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(box.getValue())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        if (this.subtitle != null) {
            graphics.drawCenteredString(this.font, Component.literal(this.subtitle), this.width / 2, 26, 0xAAAAAA);
        }

        // Inline labels for the numeric fields (drawn to the left of each box).
        if (intBoxes != null) {
            for (int i = 0; i < intEntries.size(); i++) {
                EditBox box = intBoxes[i];
                graphics.drawString(this.font, intEntries.get(i).name(),
                        box.getX() - 4 - this.font.width(intEntries.get(i).name()), box.getY() + 4, 0xCCCCCC, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
