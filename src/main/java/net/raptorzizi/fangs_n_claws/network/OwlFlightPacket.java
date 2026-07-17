package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;

import java.util.function.Supplier;

/**
 * Serverbound : le client annonce son etat de vol Owl (plane / battement) ce tick.
 * Le serveur remet la fallDistance a 0 en plane, puis rebroadcast l'etat aux joueurs
 * qui suivent l'emetteur pour qu'ils affichent l'animation ({@link OwlFlightSyncPacket}).
 */
public record OwlFlightPacket(boolean gliding, boolean flap) {

    public static void encode(OwlFlightPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.gliding());
        buf.writeBoolean(pkt.flap());
    }

    public static OwlFlightPacket decode(FriendlyByteBuf buf) {
        return new OwlFlightPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(OwlFlightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            if (pkt.gliding()) sp.resetFallDistance();
            FangsNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> sp),
                    new OwlFlightSyncPacket(sp.getId(), pkt.gliding(), pkt.flap()));
            if (sp.getItemBySlot(EquipmentSlot.CHEST).is(ItemsRegistry.OWL_CHESTPLATE.get())) {
                FncAdvancements.grant(sp, "forge/take_flight");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
