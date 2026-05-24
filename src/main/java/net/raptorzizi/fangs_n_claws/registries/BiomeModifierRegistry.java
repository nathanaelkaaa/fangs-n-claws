package net.raptorzizi.fangs_n_claws.registries;

import com.mojang.serialization.Codec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.biome.SpawnWeightsBiomeModifier;

public class BiomeModifierRegistry {

    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, FangsClawsMod.MOD_ID);

    public static final RegistryObject<Codec<SpawnWeightsBiomeModifier>> SPAWN_WEIGHTS =
            BIOME_MODIFIERS.register("spawn_weights", () -> SpawnWeightsBiomeModifier.CODEC);

    public static void register(IEventBus eventBus) {
        BIOME_MODIFIERS.register(eventBus);
    }
}
