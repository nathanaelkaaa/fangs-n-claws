package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public record TotemFrostPayload(ItemStack item) implements CustomPacketPayload {

    public static final Type<TotemFrostPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "totem_frost_anim"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TotemFrostPayload> STREAM_CODEC =
        StreamCodec.composite(
            ItemStack.STREAM_CODEC, TotemFrostPayload::item,
            TotemFrostPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(TotemFrostPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
            Minecraft.getInstance().gameRenderer.displayItemActivation(payload.item()));
    }
}
