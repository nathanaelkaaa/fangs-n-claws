package net.raptorzizi.fangs_n_claws.entity.nightmare_horse;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class NightmareHorseMoveControl extends MoveControl {

    private final NightmareHorseEntity horse;

    public NightmareHorseMoveControl(NightmareHorseEntity horse) {
        super(horse);
        this.horse = horse;
    }

    @Override
    public void tick() {
        int phase = horse.getChargePhase();

        if (phase == NightmareHorseEntity.PHASE_CHARGE) {
            horse.setSpeed(0f);
            Vec3 d = horse.getChargeDir();
            float yaw = (float) (Mth.atan2(d.z, d.x) * (180.0 / Math.PI)) - 90.0F;
            horse.setYRot(yaw);
            horse.yBodyRot = yaw;
            horse.yHeadRot = yaw;
            Vec3 m = horse.getDeltaMovement();
            horse.setDeltaMovement(d.x * NightmareHorseEntity.CHARGE_SPEED, m.y, d.z * NightmareHorseEntity.CHARGE_SPEED);

        } else if (phase == NightmareHorseEntity.PHASE_WINDUP) {
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
            horse.setDeltaMovement(-fx * NightmareHorseEntity.BACKUP_SPEED, m.y, -fz * NightmareHorseEntity.BACKUP_SPEED);

        } else {
            super.tick();
        }
    }
}
