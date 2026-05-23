package net.raptorzizi.fangs_n_claws.registries;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.biome.SpawnWeightsBiomeModifier;

public class BiomeModifierRegistry {

    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SpawnWeightsBiomeModifier>> SPAWN_WEIGHTS =
            BIOME_MODIFIERS.register("spawn_weights", () -> SpawnWeightsBiomeModifier.CODEC);

    public static void register(IEventBus eventBus) {
        BIOME_MODIFIERS.register(eventBus);
    }
}
