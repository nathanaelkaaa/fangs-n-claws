package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;

public class FangsConfigScreen extends Screen {

    private final Screen parent;

    // ── toggle state (client-side copy, written to config on Save) ──────────
    private boolean[] toggles;

    private static final String[] TOGGLE_NAMES = {
        "Goblin", "Ogre", "Golem", "Owlbear",
        "Silver Skeleton", "Evil Bat", "Ghost", "Werewolf"
    };

    // Refs kept so we can flip the label on click
    private final Button[] toggleButtons = new Button[TOGGLE_NAMES.length];

    // ── weight fields ────────────────────────────────────────────────────────
    private EditBox[] weightBoxes;

    private static final String[] WEIGHT_LABELS = {
        "Goblin", "Ogre", "Golem", "Owlbear",
        "Silver Skeleton", "Evil Bat", "Ghost", "Ghost (Nether)", "Werewolf"
    };

    private boolean serverConfigAvailable = true;

    // ── layout constants ─────────────────────────────────────────────────────
    private static final int COLS      = 3;
    private static final int COL_W     = 110;
    private static final int ROW_H     = 24;
    private static final int BTN_H     = 18;
    private static final int FIELD_H   = 12;

    public FangsConfigScreen(Screen parent) {
        super(Component.literal("Fangs 'n Claws — Configuration"));
        this.parent = parent;
    }

    // ── init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        // Load current toggle values
        toggles = new boolean[] {
            CommonConfigs.ALLOW_SPAWN_GOBLIN.get(),
            CommonConfigs.ALLOW_SPAWN_OGRE.get(),
            CommonConfigs.ALLOW_SPAWN_GOLEM.get(),
            CommonConfigs.ALLOW_SPAWN_OWLBEAR.get(),
            CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.get(),
            CommonConfigs.ALLOW_SPAWN_EVIL_BAT.get(),
            CommonConfigs.ALLOW_SPAWN_GHOST.get(),
            CommonConfigs.ALLOW_SPAWN_WEREWOLF.get()
        };

        int leftX = this.width / 2 - (COLS * COL_W) / 2;
        int y     = 32;

        // ── toggle section ───────────────────────────────────────────────────
        for (int i = 0; i < TOGGLE_NAMES.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int bx  = leftX + col * COL_W;
            int by  = y + row * ROW_H;
            int idx = i;

            toggleButtons[i] = Button.builder(toggleLabel(i), b -> {
                toggles[idx] = !toggles[idx];
                b.setMessage(toggleLabel(idx));
            }).bounds(bx, by, COL_W - 4, BTN_H).build();

            this.addRenderableWidget(toggleButtons[i]);
        }

        // rows used by toggles (8 items, 3 cols → ceil(8/3)=3 rows)
        int toggleRows   = (int) Math.ceil((double) TOGGLE_NAMES.length / COLS);
        int weightStartY = y + toggleRows * ROW_H + 18; // 18 px gap + section header

        // ── weight section ───────────────────────────────────────────────────
        weightBoxes = new EditBox[WEIGHT_LABELS.length];

        // Try to read server config values; if not loaded yet, show defaults
        int[] weightValues;
        try {
            weightValues = new int[] {
                ServerConfigs.GOBLIN_WEIGHT.get(),
                ServerConfigs.OGRE_WEIGHT.get(),
                ServerConfigs.GOLEM_WEIGHT.get(),
                ServerConfigs.OWLBEAR_WEIGHT.get(),
                ServerConfigs.SILVER_SKELETON_WEIGHT.get(),
                ServerConfigs.EVIL_BAT_WEIGHT.get(),
                ServerConfigs.GHOST_WEIGHT.get(),
                ServerConfigs.GHOST_NETHER_WEIGHT.get(),
                ServerConfigs.WEREWOLF_WEIGHT.get()
            };
            serverConfigAvailable = true;
        } catch (Exception e) {
            weightValues = new int[] { 25, 15, 5, 8, 30, 20, 25, 15, 30 };
            serverConfigAvailable = false;
        }

        for (int i = 0; i < WEIGHT_LABELS.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int fx  = leftX + col * COL_W;
            int fy  = weightStartY + row * ROW_H + 10; // +10 to leave room for label above

            EditBox box = new EditBox(
                this.font,
                fx, fy,
                COL_W - 4, FIELD_H,
                Component.literal(WEIGHT_LABELS[i])
            );
            box.setValue(String.valueOf(weightValues[i]));
            box.setMaxLength(3);
            box.setFilter(s -> s.isEmpty() || s.matches("\\d{0,3}"));
            box.setEditable(serverConfigAvailable);
            this.addRenderableWidget(box);
            weightBoxes[i] = box;
        }

        // ── bottom buttons ───────────────────────────────────────────────────
        int cx = this.width / 2;
        int bottomY = this.height - 28;

        this.addRenderableWidget(Button.builder(
            Component.literal("Save & Close"),
            b -> { saveConfig(); this.minecraft.setScreen(parent); }
        ).bounds(cx - 105, bottomY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Cancel"),
            b -> this.minecraft.setScreen(parent)
        ).bounds(cx + 5, bottomY, 100, 20).build());
    }

    // ── render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int leftX     = this.width / 2 - (COLS * COL_W) / 2;
        int toggleRows = (int) Math.ceil((double) TOGGLE_NAMES.length / COLS);
        int weightSectionTop = 32 + toggleRows * ROW_H + 18;

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Section headers
        graphics.drawString(this.font,
            Component.literal("§lSpawn Toggles"),
            leftX, 24, 0xAAAAAA, false);

        graphics.drawString(this.font,
            Component.literal("§lSpawn Weights" + (serverConfigAvailable ? "" : "  §7(join a world to edit)")),
            leftX, weightSectionTop - 10, 0xAAAAAA, false);

        // Weight field labels
        for (int i = 0; i < WEIGHT_LABELS.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int lx  = leftX + col * COL_W;
            int ly  = weightSectionTop + row * ROW_H;
            graphics.drawString(this.font, WEIGHT_LABELS[i], lx, ly, 0xCCCCCC, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Component toggleLabel(int idx) {
        String state = toggles[idx] ? "§aON" : "§cOFF";
        return Component.literal(TOGGLE_NAMES[idx] + ": " + state);
    }

    private void saveConfig() {
        // Save spawn toggles
        CommonConfigs.ALLOW_SPAWN_GOBLIN.set(toggles[0]);
        CommonConfigs.ALLOW_SPAWN_OGRE.set(toggles[1]);
        CommonConfigs.ALLOW_SPAWN_GOLEM.set(toggles[2]);
        CommonConfigs.ALLOW_SPAWN_OWLBEAR.set(toggles[3]);
        CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON.set(toggles[4]);
        CommonConfigs.ALLOW_SPAWN_EVIL_BAT.set(toggles[5]);
        CommonConfigs.ALLOW_SPAWN_GHOST.set(toggles[6]);
        CommonConfigs.ALLOW_SPAWN_WEREWOLF.set(toggles[7]);

        // Save spawn weights (only if server config is loaded)
        if (serverConfigAvailable) {
            ServerConfigs.GOBLIN_WEIGHT.set(          parseWeight(weightBoxes[0], ServerConfigs.GOBLIN_WEIGHT.get()));
            ServerConfigs.OGRE_WEIGHT.set(            parseWeight(weightBoxes[1], ServerConfigs.OGRE_WEIGHT.get()));
            ServerConfigs.GOLEM_WEIGHT.set(           parseWeight(weightBoxes[2], ServerConfigs.GOLEM_WEIGHT.get()));
            ServerConfigs.OWLBEAR_WEIGHT.set(         parseWeight(weightBoxes[3], ServerConfigs.OWLBEAR_WEIGHT.get()));
            ServerConfigs.SILVER_SKELETON_WEIGHT.set( parseWeight(weightBoxes[4], ServerConfigs.SILVER_SKELETON_WEIGHT.get()));
            ServerConfigs.EVIL_BAT_WEIGHT.set(        parseWeight(weightBoxes[5], ServerConfigs.EVIL_BAT_WEIGHT.get()));
            ServerConfigs.GHOST_WEIGHT.set(           parseWeight(weightBoxes[6], ServerConfigs.GHOST_WEIGHT.get()));
            ServerConfigs.GHOST_NETHER_WEIGHT.set(    parseWeight(weightBoxes[7], ServerConfigs.GHOST_NETHER_WEIGHT.get()));
            ServerConfigs.WEREWOLF_WEIGHT.set(        parseWeight(weightBoxes[8], ServerConfigs.WEREWOLF_WEIGHT.get()));
        }
    }

    private static int parseWeight(EditBox box, int fallback) {
        try {
            return Math.max(0, Math.min(500, Integer.parseInt(box.getValue())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
