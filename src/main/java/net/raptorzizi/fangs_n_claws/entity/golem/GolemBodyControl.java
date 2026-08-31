package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class GolemBodyControl extends BodyRotationControl {

    private static final float MAX_ROTATE   = 60f;
    private static final int   HISTORY_SIZE = 10;
    private static final int   RAMP_TICKS   = 10;

    private final Mob entity;

    private int   rotateTime    = 0;
    private float targetYawHead = 0f;

    private final double[] histPosX = new double[HISTORY_SIZE];
    private final double[] histPosZ = new double[HISTORY_SIZE];

    public GolemBodyControl(Mob entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public void clientTick() {
        for (int i = HISTORY_SIZE - 1; i > 0; i--) {
            histPosX[i] = histPosX[i - 1];
            histPosZ[i] = histPosZ[i - 1];
        }
        histPosX[0] = entity.getX();
        histPosZ[0] = entity.getZ();

        double dx    = delta(histPosX);
        double dz    = delta(histPosZ);
        double distSq = dx * dx + dz * dz;

        boolean inCombat = entity instanceof Mob mob
                && mob.getTarget() != null;

        if (distSq > 2.5e-7 && !inCombat) {
            double moveAngle = Mth.atan2(dz, dx) * (180.0 / Math.PI) - 90.0;
            entity.yBodyRot += Mth.wrapDegrees((float) moveAngle - entity.yBodyRot) * 0.15f;
            targetYawHead    = entity.yHeadRot;
            rotateTime       = 0;

        } else {
            if (Math.abs(Mth.wrapDegrees(entity.yHeadRot - targetYawHead)) > 15f) {
                rotateTime    = 0;
                targetYawHead = entity.yHeadRot;
            } else {
                rotateTime++;
            }

            float limit = MAX_ROTATE;
            if (rotateTime > RAMP_TICKS) {
                limit = Math.max(1f - (rotateTime - RAMP_TICKS) / (float) RAMP_TICKS, 0f) * MAX_ROTATE;
            }

            entity.yBodyRot = approach(entity.yHeadRot, entity.yBodyRot, limit);
        }
    }

    private double delta(double[] arr) {
        return mean(arr, 0) - mean(arr, HISTORY_SIZE / 2);
    }

    private double mean(double[] arr, int start) {
        double sum = 0;
        for (int i = 0; i < HISTORY_SIZE / 2; i++) sum += arr[i + start];
        return sum / arr.length;
    }

    private static float approach(float target, float current, float limit) {
        float delta = Mth.clamp(Mth.wrapDegrees(current - target), -limit, limit);
        return target + delta * 0.55f;
    }
}
