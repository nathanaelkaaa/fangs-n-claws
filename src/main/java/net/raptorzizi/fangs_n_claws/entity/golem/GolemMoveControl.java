package net.raptorzizi.fangs_n_claws.entity.golem;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;

public class GolemMoveControl extends MoveControl {

    public GolemMoveControl(GolemEntity golem) {
        super(golem);
    }

    @Override
    protected float rotlerp(float pAngle, float pTargetAngle, float pMaxSpeed) {
        float delta = Mth.wrapDegrees(pTargetAngle - pAngle);
        return pAngle + delta * 0.15f;
    }
}
