package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class HorseChargeMoveControl extends MoveControl {

    private final HorseMob      horse;
    private final ChargeAbility charge;

    public HorseChargeMoveControl(HorseMob horse, ChargeAbility charge) {
        super(horse);
        this.horse = horse;
        this.charge = charge;
    }

    @Override
    public void tick() {
        int phase = charge.phase();

        if (phase == ChargeAbility.CHARGE) {
            horse.setSpeed(0f);
            Vec3 d = charge.dir();
            float yaw = (float) (Mth.atan2(d.z, d.x) * (180.0 / Math.PI)) - 90.0F;
            horse.setYRot(yaw);
            horse.yBodyRot = yaw;
            horse.yHeadRot = yaw;
            Vec3 m = horse.getDeltaMovement();
            horse.setDeltaMovement(d.x * charge.speed, m.y, d.z * charge.speed);

        } else if (phase == ChargeAbility.WINDUP) {
            horse.setSpeed(0f);
            LivingEntity target = horse.getTarget();
            if (target != null) {
                double dx = target.getX() - horse.getX();
                double dz = target.getZ() - horse.getZ();
                float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                horse.setYRot(yaw);
                horse.yBodyRot = yaw;
            }
            float yawRad = horse.yBodyRot * Mth.DEG_TO_RAD;
            double fx = -Mth.sin(yawRad), fz = Mth.cos(yawRad);
            Vec3 m = horse.getDeltaMovement();
            horse.setDeltaMovement(-fx * charge.backupSpeed, m.y, -fz * charge.backupSpeed);

        } else {
            super.tick();
        }
    }
}
