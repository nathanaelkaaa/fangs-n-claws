package net.raptorzizi.fangs_n_claws.entity.werewolf;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;

public class WerewolfMoveControl extends MoveControl {

    private final WerewolfEntity werewolf;

    public WerewolfMoveControl(WerewolfEntity werewolf) {
        super(werewolf);
        this.werewolf = werewolf;
    }

    @Override
    protected float rotlerp(float pAngle, float pTargetAngle, float pMaxSpeed) {
        float angle = Mth.wrapDegrees(pTargetAngle - pAngle);
        float lerp  = werewolf.isRunning() ? 0.45f : 0.15f;
        return pAngle + angle * lerp;
    }
}
