package net.raptorzizi.fangs_n_claws.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleWormEntity;

public record PurpleWormPartsPayload(int entityId, float[] offsets, float[] headForward, float[] miniHeads)
        implements CustomPacketPayload {

    public static final Type<PurpleWormPartsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "purple_worm_parts"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PurpleWormPartsPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.entityId());
                        for (float f : payload.offsets())     buf.writeFloat(f);
                        for (float f : payload.headForward()) buf.writeFloat(f);
                        for (float f : payload.miniHeads())   buf.writeFloat(f);
                    },
                    buf -> {
                        int id = buf.readVarInt();
                        float[] arr = new float[PurpleWormEntity.PART_COUNT * 3];
                        for (int i = 0; i < arr.length; i++) arr[i] = buf.readFloat();
                        float[] fwd = new float[3];
                        for (int i = 0; i < fwd.length; i++) fwd[i] = buf.readFloat();
                        float[] mini = new float[6];
                        for (int i = 0; i < mini.length; i++) mini[i] = buf.readFloat();
                        return new PurpleWormPartsPayload(id, arr, fwd, mini);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(PurpleWormPartsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (payload.offsets().length != PurpleWormEntity.PART_COUNT * 3) return;
            if (payload.headForward().length != 3) return;
            if (payload.miniHeads().length != 6) return;
            if (!(sp.level().getEntity(payload.entityId()) instanceof PurpleWormEntity worm)) return;
            if (sp.distanceToSqr(worm) > 96.0 * 96.0) return;

            Vec3[] result = new Vec3[PurpleWormEntity.PART_COUNT];
            for (int i = 0; i < result.length; i++) {
                result[i] = new Vec3(payload.offsets()[i * 3],
                        payload.offsets()[i * 3 + 1],
                        payload.offsets()[i * 3 + 2]);
            }
            worm.setAnimatedPartOffsets(result);

            Vec3 fwd = new Vec3(payload.headForward()[0], payload.headForward()[1], payload.headForward()[2]);
            if (fwd.lengthSqr() > 1.0e-6) worm.setBreathHeadForward(fwd.normalize());

            Vec3 miniL = new Vec3(payload.miniHeads()[0], payload.miniHeads()[1], payload.miniHeads()[2]);
            Vec3 miniR = new Vec3(payload.miniHeads()[3], payload.miniHeads()[4], payload.miniHeads()[5]);
            worm.setMiniHeadOffsets(miniL, miniR);
        });
    }
}
