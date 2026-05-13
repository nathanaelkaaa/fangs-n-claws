package net.raptorzizi.fangs_n_claws.entity.owlbear;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;

public class OwlbearMoveControl extends MoveControl {

    private final OwlbearEntity owlbear;

    public OwlbearMoveControl(OwlbearEntity owlbear) {
        super(owlbear);
        this.owlbear = owlbear;
    }

    @Override
    protected float rotlerp(float pAngle, float pTargetAngle, float pMaxSpeed) {
        float angle = Mth.wrapDegrees(pTargetAngle - pAngle);
        float lerp  = owlbear.isRunning() ? 0.45f : 0.15f;
        return pAngle + angle * lerp;
    }
}
