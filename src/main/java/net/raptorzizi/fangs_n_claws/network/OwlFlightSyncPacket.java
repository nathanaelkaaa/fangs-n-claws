package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Clientbound : etat de vol Owl d'un autre joueur, pour afficher son animation.
 */
public record OwlFlightSyncPacket(int entityId, boolean gliding, boolean flap) {

    public static void encode(OwlFlightSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entityId());
        buf.writeBoolean(pkt.gliding());
        buf.writeBoolean(pkt.flap());
    }

    public static OwlFlightSyncPacket decode(FriendlyByteBuf buf) {
        return new OwlFlightSyncPacket(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(OwlFlightSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        // Corps client-only : OwlFlightState n'est charge que cote client (DistExecutor),
        // jamais sur un serveur dedie ou seul l'enregistrement du message a lieu.
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        net.raptorzizi.fangs_n_claws.client.OwlFlightState.setRemote(
                                pkt.entityId(), pkt.gliding(), pkt.flap())));
        ctx.get().setPacketHandled(true);
    }
}
