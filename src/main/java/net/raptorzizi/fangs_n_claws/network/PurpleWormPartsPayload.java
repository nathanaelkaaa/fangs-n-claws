package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleWormEntity;

import java.util.function.Supplier;

/**
 * Serverbound : le client calcule (depuis l'animation GeckoLib) la position des segments/tête du
 * Purple Worm et les envoie au serveur pour qu'il place les hitboxes multipart. 1.20.1 : SimpleChannel
 * (le nom "Payload" est conservé pour coller au code appelant partagé avec la 1.21).
 */
public record PurpleWormPartsPayload(int entityId, float[] offsets, float[] headForward, float[] miniHeads) {

    public static void encode(PurpleWormPartsPayload pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entityId());
        for (float f : pkt.offsets())     buf.writeFloat(f);
        for (float f : pkt.headForward()) buf.writeFloat(f);
        for (float f : pkt.miniHeads())   buf.writeFloat(f);
    }

    public static PurpleWormPartsPayload decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        float[] arr = new float[PurpleWormEntity.PART_COUNT * 3];
        for (int i = 0; i < arr.length; i++) arr[i] = buf.readFloat();
        float[] fwd = new float[3];
        for (int i = 0; i < fwd.length; i++) fwd[i] = buf.readFloat();
        float[] mini = new float[6];
        for (int i = 0; i < mini.length; i++) mini[i] = buf.readFloat();
        return new PurpleWormPartsPayload(id, arr, fwd, mini);
    }

    public static void handle(PurpleWormPartsPayload pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            if (pkt.offsets().length != PurpleWormEntity.PART_COUNT * 3) return;
            if (pkt.headForward().length != 3) return;
            if (pkt.miniHeads().length != 6) return;
            if (!(sp.level().getEntity(pkt.entityId()) instanceof PurpleWormEntity worm)) return;
            if (sp.distanceToSqr(worm) > 96.0 * 96.0) return;

            Vec3[] result = new Vec3[PurpleWormEntity.PART_COUNT];
            for (int i = 0; i < result.length; i++) {
                result[i] = new Vec3(pkt.offsets()[i * 3],
                        pkt.offsets()[i * 3 + 1],
                        pkt.offsets()[i * 3 + 2]);
            }
            worm.setAnimatedPartOffsets(result);

            Vec3 fwd = new Vec3(pkt.headForward()[0], pkt.headForward()[1], pkt.headForward()[2]);
            if (fwd.lengthSqr() > 1.0e-6) worm.setBreathHeadForward(fwd.normalize());

            Vec3 miniL = new Vec3(pkt.miniHeads()[0], pkt.miniHeads()[1], pkt.miniHeads()[2]);
            Vec3 miniR = new Vec3(pkt.miniHeads()[3], pkt.miniHeads()[4], pkt.miniHeads()[5]);
            worm.setMiniHeadOffsets(miniL, miniR);
        });
        ctx.get().setPacketHandled(true);
    }
}
