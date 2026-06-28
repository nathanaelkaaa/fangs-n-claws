package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class HorseFlyMoveControl extends MoveControl {

    private static final double ARRIVAL_THRESHOLD = 0.5;
    private static final double MAX_SPEED         = 0.30;
    private static final double STEER             = 0.5;
    private static final double IDLE_BRAKE        = 0.8;

    private final FlyingHorseMob horse;

    public HorseFlyMoveControl(FlyingHorseMob horse) {
        super(horse);
        this.horse = horse;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            this.horse.setDeltaMovement(this.horse.getDeltaMovement().scale(IDLE_BRAKE));
            return;
        }
        this.operation = Operation.WAIT;

        double dx = this.wantedX - this.horse.getX();
        double dy = this.wantedY - this.horse.getY();
        double dz = this.wantedZ - this.horse.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < ARRIVAL_THRESHOLD) {
            this.horse.setDeltaMovement(this.horse.getDeltaMovement().scale(0.5));
            return;
        }

        double speed = Math.min(this.speedModifier * this.horse.getAttributeValue(Attributes.FLYING_SPEED), MAX_SPEED);

        Vec3 desired = new Vec3(dx, dy, dz).scale(speed / dist);
        Vec3 current = this.horse.getDeltaMovement();
        this.horse.setDeltaMovement(current.add(desired.subtract(current).scale(STEER)));

        if (dx * dx + dz * dz > 1.0E-4) {
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
            this.horse.setYRot(this.rotlerp(this.horse.getYRot(), yaw, 10.0F));
            this.horse.yBodyRot = this.horse.getYRot();
        }
    }
}
