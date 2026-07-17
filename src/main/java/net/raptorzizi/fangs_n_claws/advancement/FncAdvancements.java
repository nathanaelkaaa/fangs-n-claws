package net.raptorzizi.fangs_n_claws.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public final class FncAdvancements {

    public static final String CODE = "code";

    private FncAdvancements() {}

    public static void grant(ServerPlayer player, String path) {
        grant(player, path, CODE);
    }

    public static void grant(ServerPlayer player, String path, String criterion) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Advancement advancement = server.getAdvancements().getAdvancement(FangsClawsMod.id(path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
