package net.raptorzizi.fangs_n_claws.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.raptorzizi.fangs_n_claws.item.armor.OwlArmorItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OwlFlightState {
    private OwlFlightState() {}

    public static final int FLAP_DURATION = 10;

    private static long clientTick = 0;

    private static boolean localGliding = false;
    private static long    localFlapEndTick = Long.MIN_VALUE;

    private static final Map<Integer, RemoteState> REMOTE = new ConcurrentHashMap<>();

    private static final class RemoteState {
        boolean gliding = false;
        long flapEndTick = Long.MIN_VALUE;
    }

    private static final float[] FLAP_TIMES  = { 0f, 0.25f, 0.5f, 1f };
    private static final float[][] FLAP_RIGHT = {
            { -12.5f,   0f,   90f },
            { -12.5f,  30f,  130f },
            { -40f,   -10f,  -30f },
            { -12.5f,   0f,   90f },
    };

    private static final float[] GLIDE_RIGHT = { -12.5f, 0f, 90f };

    public static void tickLocal()               { clientTick++; }
    public static void setLocalGliding(boolean g) { localGliding = g; }
    public static void triggerLocalFlap()         { localFlapEndTick = clientTick + FLAP_DURATION; }

    private static final float[] WING_TIMES  = { 0f,  0.30f, 0.55f, 1f };
    private static final float[] WING_VALUES = { 0f,  0.7f, -1.2f,  0f };

    public static float flapWingSweep(Player player) {
        float p = flapProgress(player);
        if (p < 0f) return 0f;
        for (int i = 0; i < WING_TIMES.length - 1; i++) {
            if (p <= WING_TIMES[i + 1]) {
                float f = (p - WING_TIMES[i]) / (WING_TIMES[i + 1] - WING_TIMES[i]);
                return Mth.lerp(f, WING_VALUES[i], WING_VALUES[i + 1]);
            }
        }
        return 0f;
    }

    private static float flapProgress(Player player) {
        long remaining;
        if (player == Minecraft.getInstance().player) {
            remaining = localFlapEndTick - clientTick;
        } else {
            RemoteState rs = REMOTE.get(player.getId());
            if (rs == null) return -1f;
            remaining = rs.flapEndTick - clientTick;
        }
        if (remaining <= 0) return -1f;
        return 1f - Math.min(remaining, FLAP_DURATION) / (float) FLAP_DURATION;
    }

    public static void setRemote(int entityId, boolean gliding, boolean flap) {
        RemoteState rs = REMOTE.computeIfAbsent(entityId, k -> new RemoteState());
        rs.gliding = gliding;
        if (flap) rs.flapEndTick = clientTick + FLAP_DURATION;
        if (!gliding && clientTick >= rs.flapEndTick) REMOTE.remove(entityId);
    }

    public static boolean applyArmPose(PlayerModel<?> model, Player player, float ageInTicks) {
        if (!OwlArmorItem.hasOwlChestplate(player)) return false;

        boolean gliding;
        long flapRemaining;
        if (player == Minecraft.getInstance().player) {
            gliding = localGliding;
            flapRemaining = localFlapEndTick - clientTick;
        } else {
            RemoteState rs = REMOTE.get(player.getId());
            if (rs == null) return false;
            gliding = rs.gliding;
            flapRemaining = rs.flapEndTick - clientTick;
        }

        float[] rightArm;
        if (flapRemaining > 0) {
            float p = 1f - Math.min(flapRemaining, FLAP_DURATION) / (float) FLAP_DURATION; // 0 -> 1
            rightArm = sampleFlap(p);
        } else if (gliding) {
            rightArm = GLIDE_RIGHT.clone();
            rightArm[2] += Mth.cos(ageInTicks * 0.1f) * 4f;
        } else {
            return false;
        }

        applyArm(model, rightArm);
        applyLegs(model, ageInTicks);
        copyOverlays(model);
        return true;
    }

    private static float[] sampleFlap(float p) {
        for (int i = 0; i < FLAP_TIMES.length - 1; i++) {
            float t0 = FLAP_TIMES[i], t1 = FLAP_TIMES[i + 1];
            if (p <= t1) {
                float f = (p - t0) / (t1 - t0);
                float[] a = FLAP_RIGHT[i], b = FLAP_RIGHT[i + 1];
                return new float[] {
                        Mth.lerp(f, a[0], b[0]),
                        Mth.lerp(f, a[1], b[1]),
                        Mth.lerp(f, a[2], b[2]),
                };
            }
        }
        return FLAP_RIGHT[FLAP_RIGHT.length - 1].clone();
    }

    private static void applyArm(PlayerModel<?> model, float[] rightDeg) {
        float xr = (float) Math.toRadians(rightDeg[0]);
        float yr = (float) Math.toRadians(rightDeg[1]);
        float zr = -(float) Math.toRadians(rightDeg[2]);

        model.rightArm.xRot = xr;
        model.rightArm.yRot = -yr;
        model.rightArm.zRot = -zr;

        model.leftArm.xRot =  xr;
        model.leftArm.yRot = yr;
        model.leftArm.zRot = zr;
    }

    private static void applyLegs(PlayerModel<?> model, float t) {
        float amp = (float) Math.toRadians(9);
        float wob = (float) Math.toRadians(4);

        model.rightLeg.yRot = 0f; model.rightLeg.zRot = 0f;
        model.leftLeg.yRot  = 0f; model.leftLeg.zRot  = 0f;

        model.rightLeg.xRot = Mth.sin(t * 0.10f)        * amp + Mth.sin(t * 0.043f)        * wob;
        model.leftLeg.xRot  = Mth.sin(t * 0.10f + 2.1f) * amp + Mth.sin(t * 0.051f + 1.3f) * wob;
    }

    private static void copyOverlays(PlayerModel<?> model) {
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.jacket.copyFrom(model.body);
    }
}
