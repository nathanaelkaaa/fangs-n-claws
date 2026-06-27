package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public record NightmareBoostPayload(boolean boosting) implements CustomPacketPayload {

    public static final Type<NightmareBoostPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "nightmare_boost"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NightmareBoostPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NightmareBoostPayload::boosting,
            NightmareBoostPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
