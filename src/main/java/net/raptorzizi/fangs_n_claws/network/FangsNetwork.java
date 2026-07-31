package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.Optional;

public class FangsNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FangsClawsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TotemFrostPacket.class,
                TotemFrostPacket::encode, TotemFrostPacket::decode, TotemFrostPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, OwlFlightPacket.class,
                OwlFlightPacket::encode, OwlFlightPacket::decode, OwlFlightPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, OwlFlightSyncPacket.class,
                OwlFlightSyncPacket::encode, OwlFlightSyncPacket::decode, OwlFlightSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, PurpleWormPartsPayload.class,
                PurpleWormPartsPayload::encode, PurpleWormPartsPayload::decode, PurpleWormPartsPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
