package net.raptorzizi.fangs_n_claws.entity.ogre;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;

public class OgreMoveControl extends MoveControl {

    private final OgreEntity ogre;

    public OgreMoveControl(OgreEntity ogre) {
        super(ogre);
        this.ogre = ogre;
    }

    @Override
    protected float rotlerp(float pAngle, float pTargetAngle, float pMaxSpeed) {
        float angle = Mth.wrapDegrees(pTargetAngle - pAngle);
        float lerp = ogre.isRunning() ? 0.35f : 0.12f;
        return pAngle + angle * lerp;
    }
}
