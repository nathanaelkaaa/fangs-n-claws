package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

/**
 * Serverbound : le client annonce son etat de vol Owl (plane / battement) ce tick.
 * Le serveur remet la fallDistance a 0 en plane, puis rebroadcast l'etat aux joueurs
 * qui suivent l'emetteur pour qu'ils affichent l'animation ({@link OwlFlightSyncPayload}).
 */
public record OwlFlightPayload(boolean gliding, boolean flap) implements CustomPacketPayload {

    public static final Type<OwlFlightPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "owl_flight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OwlFlightPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, OwlFlightPayload::gliding,
            ByteBufCodecs.BOOL, OwlFlightPayload::flap,
            OwlFlightPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(OwlFlightPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                if (payload.gliding()) sp.resetFallDistance();
                PacketDistributor.sendToPlayersTrackingEntity(sp,
                    new OwlFlightSyncPayload(sp.getId(), payload.gliding(), payload.flap()));
            }
        });
    }
}
