package net.raptorzizi.fangs_n_claws.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    static {
        SPEC = BUILDER.build();
    }
}