package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;


public class OwlbearFlyMoveControl extends MoveControl {

    private final OwlbearEntity owlbear;

    private static final double ARRIVAL_THRESHOLD = 0.5;
    private static final double ARRIVAL_BRAKE = 0.5;

    public OwlbearFlyMoveControl(OwlbearEntity owlbear) {
        super(owlbear);
        this.owlbear = owlbear;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) return;
        if (owlbear.getDiveState() != OwlbearEntity.DIVE_NONE) return;

        Vec3 toTarget = new Vec3(
                this.wantedX - owlbear.getX(),
                this.wantedY - owlbear.getY(),
                this.wantedZ - owlbear.getZ());

        double dist = toTarget.length();

        if (dist < ARRIVAL_THRESHOLD) {
            this.operation = Operation.WAIT;
            owlbear.setDeltaMovement(owlbear.getDeltaMovement().scale(ARRIVAL_BRAKE));
            return;
        }

        owlbear.setDeltaMovement(
                owlbear.getDeltaMovement().add(toTarget.scale(this.speedModifier * 0.05 / dist)));

        Vec3 motion = owlbear.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-6) {
            owlbear.setYRot(-((float) Mth.atan2(motion.x, motion.z)) * Mth.RAD_TO_DEG);
            owlbear.yBodyRot = owlbear.getYRot();
        }
    }
}
