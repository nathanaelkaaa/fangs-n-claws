package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.raptorzizi.fangs_n_claws.config.CommonConfigs;
import net.raptorzizi.fangs_n_claws.config.ServerConfigs;

import java.util.ArrayList;
import java.util.List;

/**
 * Landing page for the mod config. Splits the settings into categories
 * (Spawn Toggles / Spawn Rates / Behavior), each reachable through a button
 * that opens a dedicated sub-page — mirroring the sectioned layout of the
 * 1.21.1 NeoForge auto-generated config screen. A single "Reset to Defaults"
 * button restores every category at once (disabled when nothing differs).
 */
public class FangsConfigScreen extends Screen {

    private final Screen parent;

    private final List<FangsToggleListScreen.Entry> spawnToggles    = buildSpawnToggles();
    private final List<FangsToggleListScreen.Entry> behaviorToggles = buildBehaviorToggles();
    private final List<FangsToggleListScreen.IntEntry> behaviorInts = buildBehaviorInts();
    private final List<FangsToggleListScreen.IntEntry> dimensionCaps = buildDimensionCaps();
    private final List<FangsWeightListScreen.Entry> spawnWeights    = buildSpawnWeights();

    private Button resetButton;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int GAP   = 24;

    public FangsConfigScreen(Screen parent) {
        super(Component.literal("Fangs 'n Claws — Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        int y  = this.height / 2 - 72;

        this.addRenderableWidget(Button.builder(Component.literal("Spawn Toggles"),
                b -> this.minecraft.setScreen(new FangsToggleListScreen(
                        this, Component.literal("Spawn Toggles"), spawnToggles)))
                .bounds(cx - BTN_W / 2, y, BTN_W, BTN_H).build());

        this.addRenderableWidget(Button.builder(Component.literal("Spawn Rates"),
                b -> this.minecraft.setScreen(new FangsWeightListScreen(
                        this, Component.literal("Spawn Rates"), spawnWeights)))
                .bounds(cx - BTN_W / 2, y + GAP, BTN_W, BTN_H).build());

        this.addRenderableWidget(Button.builder(Component.literal("Spawn Limits (per dimension)"),
                b -> this.minecraft.setScreen(new FangsToggleListScreen(
                        this, Component.literal("Spawn Limits"),
                        "§7Per dimension: -1 = unlimited, 0 = no F&C spawns, N = density cap",
                        List.of(), dimensionCaps)))
                .bounds(cx - BTN_W / 2, y + GAP * 2, BTN_W, BTN_H).build());

        this.addRenderableWidget(Button.builder(Component.literal("Behavior"),
                b -> this.minecraft.setScreen(new FangsToggleListScreen(
                        this, Component.literal("Behavior"), behaviorToggles, behaviorInts)))
                .bounds(cx - BTN_W / 2, y + GAP * 3, BTN_W, BTN_H).build());

        this.resetButton = Button.builder(Component.literal("Reset to Defaults"),
                b -> { resetAll(); b.active = false; })
                .bounds(cx - BTN_W / 2, y + GAP * 4 + 8, BTN_W, BTN_H).build();
        this.resetButton.active = hasChanges();
        this.addRenderableWidget(this.resetButton);

        this.addRenderableWidget(Button.builder(Component.literal("Done"),
                b -> this.minecraft.setScreen(parent))
                .bounds(cx - BTN_W / 2, y + GAP * 5 + 12, BTN_W, BTN_H).build());
    }

    // ── reset / change detection ────────────────────────────────────────────────

    private static boolean weightsAvailable() {
        // ServerConfigs is a SERVER config — only loaded inside a world.
        try {
            ServerConfigs.GOBLIN_WEIGHT.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasChanges() {
        for (FangsToggleListScreen.Entry e : spawnToggles)    if (e.cfg().get() != e.def()) return true;
        for (FangsToggleListScreen.Entry e : behaviorToggles) if (e.cfg().get() != e.def()) return true;
        for (FangsToggleListScreen.IntEntry e : behaviorInts) if (e.cfg().get() != e.def()) return true;
        for (FangsToggleListScreen.IntEntry e : dimensionCaps) if (e.cfg().get() != e.def()) return true;
        if (weightsAvailable()) {
            for (FangsWeightListScreen.Entry e : spawnWeights) if (e.cfg().get() != e.def()) return true;
        }
        return false;
    }

    private void resetAll() {
        for (FangsToggleListScreen.Entry e : spawnToggles)    e.cfg().set(e.def());
        for (FangsToggleListScreen.Entry e : behaviorToggles) e.cfg().set(e.def());
        for (FangsToggleListScreen.IntEntry e : behaviorInts) e.cfg().set(e.def());
        for (FangsToggleListScreen.IntEntry e : dimensionCaps) e.cfg().set(e.def());
        if (weightsAvailable()) {
            for (FangsWeightListScreen.Entry e : spawnWeights) e.cfg().set(e.def());
        }
    }

    // ── category contents ─────────────────────────────────────────────────────

    private static List<FangsToggleListScreen.Entry> buildSpawnToggles() {
        return List.of(
                new FangsToggleListScreen.Entry("Goblin",          CommonConfigs.ALLOW_SPAWN_GOBLIN,          true),
                new FangsToggleListScreen.Entry("Ogre",            CommonConfigs.ALLOW_SPAWN_OGRE,            true),
                new FangsToggleListScreen.Entry("Cave Ogre",       CommonConfigs.ALLOW_SPAWN_CAVE_OGRE,       true),
                new FangsToggleListScreen.Entry("Golem",           CommonConfigs.ALLOW_SPAWN_GOLEM,           true),
                new FangsToggleListScreen.Entry("Owlbear",         CommonConfigs.ALLOW_SPAWN_OWLBEAR,         true),
                new FangsToggleListScreen.Entry("Shrike",          CommonConfigs.ALLOW_SPAWN_SHRIKE,          true),
                new FangsToggleListScreen.Entry("Silver Skeleton", CommonConfigs.ALLOW_SPAWN_SILVER_SKELETON, true),
                new FangsToggleListScreen.Entry("Evil Bat",        CommonConfigs.ALLOW_SPAWN_EVIL_BAT,        true),
                new FangsToggleListScreen.Entry("Ghost",           CommonConfigs.ALLOW_SPAWN_GHOST,           true),
                new FangsToggleListScreen.Entry("Werewolf",        CommonConfigs.ALLOW_SPAWN_WEREWOLF,        true),
                new FangsToggleListScreen.Entry("Dart Goblin",     CommonConfigs.ALLOW_SPAWN_DART_GOBLIN,     true),
                new FangsToggleListScreen.Entry("Imp",             CommonConfigs.ALLOW_SPAWN_IMP,             true),
                new FangsToggleListScreen.Entry("Hell Ogre",       CommonConfigs.ALLOW_SPAWN_HELL_OGRE,       true),
                new FangsToggleListScreen.Entry("Scorpion",        CommonConfigs.ALLOW_SPAWN_SCORPION,        true),
                new FangsToggleListScreen.Entry("Ice Golem",       CommonConfigs.ALLOW_SPAWN_ICE_GOLEM,       true),
                new FangsToggleListScreen.Entry("Fire Ghost",      CommonConfigs.ALLOW_SPAWN_FIRE_GHOST,      true),
                new FangsToggleListScreen.Entry("Desert Scorpion", CommonConfigs.ALLOW_SPAWN_DESERT_SCORPION, true),
                new FangsToggleListScreen.Entry("Frost Scorpion",  CommonConfigs.ALLOW_SPAWN_FROST_SCORPION,  true),
                new FangsToggleListScreen.Entry("Nether Scorpion", CommonConfigs.ALLOW_SPAWN_NETHER_SCORPION, true),
                new FangsToggleListScreen.Entry("Horse Bat",       CommonConfigs.ALLOW_SPAWN_HORSE_BAT,       true),
                new FangsToggleListScreen.Entry("Wild Wolf",       CommonConfigs.ALLOW_SPAWN_WILD_WOLF,       true),
                new FangsToggleListScreen.Entry("Nightmare Horse", CommonConfigs.ALLOW_SPAWN_NIGHTMARE_HORSE, true),
                new FangsToggleListScreen.Entry("Mimic",           CommonConfigs.ALLOW_SPAWN_MIMIC,           true)
        );
    }

    private static List<FangsToggleListScreen.Entry> buildBehaviorToggles() {
        return List.of(
                new FangsToggleListScreen.Entry("Vanilla Skeleton Horse", CommonConfigs.VANILLA_SKELETON_HORSE,             false),
                new FangsToggleListScreen.Entry("Vanilla Zombie Horse",   CommonConfigs.VANILLA_ZOMBIE_HORSE,               false),
                new FangsToggleListScreen.Entry("Natural Skeleton Horse", CommonConfigs.ALLOW_NATURAL_SPAWN_SKELETON_HORSE, true),
                new FangsToggleListScreen.Entry("Natural Zombie Horse",   CommonConfigs.ALLOW_NATURAL_SPAWN_ZOMBIE_HORSE,   true),
                new FangsToggleListScreen.Entry("Goblin Stealing",        CommonConfigs.ALLOW_GOBLIN_STEALING,              true)
        );
    }

    private static List<FangsToggleListScreen.IntEntry> buildBehaviorInts() {
        return List.of(
                // 1/x chance a structure loot chest becomes a Mimic (higher = fewer; huge = disabled).
                new FangsToggleListScreen.IntEntry("Mimic Spawn Chance (1/x)",
                        CommonConfigs.MIMIC_SPAWN_CHANCE, 20, 1, 100000)
        );
    }

    // Per-dimension mob-density caps (CommonConfigs.DIMENSION_CAPS). Twilight Forest is only
    // present in the map when the mod is loaded, so we add each entry only if it exists.
    private static List<FangsToggleListScreen.IntEntry> buildDimensionCaps() {
        List<FangsToggleListScreen.IntEntry> list = new ArrayList<>();
        addCap(list, "minecraft:overworld",            "Overworld",       40);
        addCap(list, "minecraft:the_nether",           "Nether",          20);
        addCap(list, "twilightforest:twilight_forest", "Twilight Forest", 15);
        return list;
    }

    private static void addCap(List<FangsToggleListScreen.IntEntry> list, String dimId, String label, int def) {
        ForgeConfigSpec.IntValue cfg = CommonConfigs.DIMENSION_CAPS.get(new ResourceLocation(dimId));
        if (cfg != null) list.add(new FangsToggleListScreen.IntEntry(label, cfg, def, -1, 100000));
    }

    private static List<FangsWeightListScreen.Entry> buildSpawnWeights() {
        return List.of(
                new FangsWeightListScreen.Entry("Goblin",             ServerConfigs.GOBLIN_WEIGHT,          20),
                new FangsWeightListScreen.Entry("Ogre",               ServerConfigs.OGRE_WEIGHT,            15),
                new FangsWeightListScreen.Entry("Cave Ogre",          ServerConfigs.CAVE_OGRE_WEIGHT,       15),
                new FangsWeightListScreen.Entry("Golem",              ServerConfigs.GOLEM_WEIGHT,            5),
                new FangsWeightListScreen.Entry("Owlbear",            ServerConfigs.OWLBEAR_WEIGHT,          8),
                new FangsWeightListScreen.Entry("Shrike",             ServerConfigs.SHRIKE_WEIGHT,           5),
                new FangsWeightListScreen.Entry("Silver Skeleton",    ServerConfigs.SILVER_SKELETON_WEIGHT, 20),
                new FangsWeightListScreen.Entry("Evil Bat",           ServerConfigs.EVIL_BAT_WEIGHT,        20),
                new FangsWeightListScreen.Entry("Ghost",              ServerConfigs.GHOST_WEIGHT,           25),
                new FangsWeightListScreen.Entry("Ghost (Nether)",     ServerConfigs.GHOST_NETHER_WEIGHT,    15),
                new FangsWeightListScreen.Entry("Werewolf",           ServerConfigs.WEREWOLF_WEIGHT,        25),
                new FangsWeightListScreen.Entry("Dart Goblin",        ServerConfigs.DART_GOBLIN_WEIGHT,      5),
                new FangsWeightListScreen.Entry("Imp",                ServerConfigs.IMP_WEIGHT,             10),
                new FangsWeightListScreen.Entry("Hell Ogre",          ServerConfigs.HELL_OGRE_WEIGHT,       10),
                new FangsWeightListScreen.Entry("Scorpion",           ServerConfigs.SCORPION_WEIGHT,        25),
                new FangsWeightListScreen.Entry("Ice Golem",          ServerConfigs.ICE_GOLEM_WEIGHT,        5),
                new FangsWeightListScreen.Entry("Fire Ghost",         ServerConfigs.FIRE_GHOST_WEIGHT,      10),
                new FangsWeightListScreen.Entry("Fire Ghost (Nether)",ServerConfigs.FIRE_GHOST_NETHER_WEIGHT,12),
                new FangsWeightListScreen.Entry("Desert Scorpion",    ServerConfigs.DESERT_SCORPION_WEIGHT, 20),
                new FangsWeightListScreen.Entry("Frost Scorpion",     ServerConfigs.FROST_SCORPION_WEIGHT,  15),
                new FangsWeightListScreen.Entry("Nether Scorpion",    ServerConfigs.NETHER_SCORPION_WEIGHT, 15),
                new FangsWeightListScreen.Entry("Horse Bat",          ServerConfigs.HORSE_BAT_WEIGHT,        3),
                new FangsWeightListScreen.Entry("Wild Wolf",          ServerConfigs.WILD_WOLF_WEIGHT,        8),
                new FangsWeightListScreen.Entry("Nightmare Horse",    ServerConfigs.NIGHTMARE_HORSE_WEIGHT,  8),
                new FangsWeightListScreen.Entry("Skeleton Horse",     ServerConfigs.SKELETON_HORSE_WEIGHT,   3),
                new FangsWeightListScreen.Entry("Zombie Horse",       ServerConfigs.ZOMBIE_HORSE_WEIGHT,     3)
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 88, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.literal("§7Choose a category"), this.width / 2, this.height / 2 - 74, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
