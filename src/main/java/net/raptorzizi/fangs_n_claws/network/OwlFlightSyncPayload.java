package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.client.OwlFlightState;

/**
 * Clientbound : etat de vol Owl d'un autre joueur, pour afficher son animation.
 */
public record OwlFlightSyncPayload(int entityId, boolean gliding, boolean flap) implements CustomPacketPayload {

    public static final Type<OwlFlightSyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "owl_flight_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OwlFlightSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OwlFlightSyncPayload::entityId,
            ByteBufCodecs.BOOL,    OwlFlightSyncPayload::gliding,
            ByteBufCodecs.BOOL,    OwlFlightSyncPayload::flap,
            OwlFlightSyncPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Corps client-only : OwlFlightState n'est charge que lorsque cette methode s'execute (cote client),
    // jamais sur un serveur dedie ou seul l'enregistrement du type a lieu.
    public static void handleClient(OwlFlightSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
            OwlFlightState.setRemote(payload.entityId(), payload.gliding(), payload.flap()));
    }
}
