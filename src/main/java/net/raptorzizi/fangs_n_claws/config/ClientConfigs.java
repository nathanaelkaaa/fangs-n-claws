package net.raptorzizi.fangs_n_claws.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        SPEC = BUILDER.build();
    }
}