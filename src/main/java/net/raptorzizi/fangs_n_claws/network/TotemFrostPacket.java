package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TotemFrostPacket(ItemStack item) {

    public static void encode(TotemFrostPacket pkt, FriendlyByteBuf buf) {
        buf.writeItem(pkt.item());
    }

    public static TotemFrostPacket decode(FriendlyByteBuf buf) {
        return new TotemFrostPacket(buf.readItem());
    }

    public static void handle(TotemFrostPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        net.minecraft.client.Minecraft.getInstance().gameRenderer
                                .displayItemActivation(pkt.item())));
        ctx.get().setPacketHandled(true);
    }
}
