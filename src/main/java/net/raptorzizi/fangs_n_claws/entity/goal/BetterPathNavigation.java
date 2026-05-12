package net.raptorzizi.fangs_n_claws.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class BetterPathNavigation extends GroundPathNavigation {

    public BetterPathNavigation(Mob mob, Level world) {
        super(mob, world);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected void followThePath() {
        Path path = Objects.requireNonNull(this.path);
        Vec3 entityPos = this.getTempMobPos();
        int pathLength = path.getNodeCount();

        for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
            if (path.getNode(i).y != Math.floor(entityPos.y)) {
                pathLength = i;
                break;
            }
        }

        final Vec3 base = entityPos.add(-this.mob.getBbWidth() * 0.5F, 0.0F, -this.mob.getBbWidth() * 0.5F);
        final Vec3 max  = base.add(this.mob.getBbWidth(), this.mob.getBbHeight(), this.mob.getBbWidth());

        if (this.tryShortcut(path, entityPos, pathLength, base, max)) {
            if (this.isAt(path, 0.5F) || this.atElevationChange(path) && this.isAt(path, this.mob.getBbWidth() * 0.5F)) {
                path.setNextNodeIndex(path.getNextNodeIndex() + 1);
            }
        }

        this.doStuckDetection(entityPos);
    }

    private boolean isAt(Path path, float threshold) {
        final Vec3 pathPos = path.getNextEntityPos(this.mob);
        return Mth.abs((float)(this.mob.getX() - pathPos.x)) < threshold
            && Mth.abs((float)(this.mob.getZ() - pathPos.z)) < threshold
            && Math.abs(this.mob.getY() - pathPos.y) < 1.0D;
    }

    private boolean atElevationChange(Path path) {
        final int curr  = path.getNextNodeIndex();
        final int end   = Math.min(path.getNodeCount(), curr + Mth.ceil(this.mob.getBbWidth() * 0.5F) + 1);
        final int currY = path.getNode(curr).y;
        for (int i = curr + 1; i < end; i++) {
            if (path.getNode(i).y != currY) return true;
        }
        return false;
    }

    private boolean tryShortcut(Path path, Vec3 entityPos, int pathLength, Vec3 base, Vec3 max) {
        for (int i = pathLength; --i > path.getNextNodeIndex(); ) {
            final Vec3 vec = path.getEntityPosAtNode(this.mob, i).subtract(entityPos);
            if (this.sweep(vec, base, max)) {
                path.setNextNodeIndex(i);
                return false;
            }
        }
        return true;
    }

    // ---- AABB sweep (https://github.com/andyhall/voxel-aabb-sweep) ----

    private static final float EPSILON = 1.0E-8F;

    private boolean sweep(Vec3 vec, Vec3 base, Vec3 max) {
        float t = 0.0F;
        float max_t = (float) vec.length();
        if (max_t < EPSILON) return true;

        final float[] tr     = new float[3];
        final int[]   ldi    = new int[3];
        final int[]   tri    = new int[3];
        final int[]   step   = new int[3];
        final float[] tDelta = new float[3];
        final float[] tNext  = new float[3];
        final float[] normed = new float[3];

        for (int i = 0; i < 3; i++) {
            float value = element(vec, i);
            boolean dir = value >= 0.0F;
            step[i]   = dir ? 1 : -1;
            float lead = element(dir ? max : base, i);
            tr[i]     = element(dir ? base : max, i);
            ldi[i]    = leadEdgeToInt(lead, step[i]);
            tri[i]    = trailEdgeToInt(tr[i], step[i]);
            normed[i] = value / max_t;
            tDelta[i] = Mth.abs(max_t / value);
            float dist = dir ? (ldi[i] + 1 - lead) : (lead - ldi[i]);
            tNext[i]  = tDelta[i] < Float.POSITIVE_INFINITY ? tDelta[i] * dist : Float.POSITIVE_INFINITY;
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        do {
            int axis = (tNext[0] < tNext[1]) ? ((tNext[0] < tNext[2]) ? 0 : 2)
                                              : ((tNext[1] < tNext[2]) ? 1 : 2);
            float dt = tNext[axis] - t;
            t = tNext[axis];
            ldi[axis] += step[axis];
            tNext[axis] += tDelta[axis];
            for (int i = 0; i < 3; i++) {
                tr[i] += dt * normed[i];
                tri[i] = trailEdgeToInt(tr[i], step[i]);
            }

            int stepx = step[0], x0 = (axis == 0) ? ldi[0] : tri[0], x1 = ldi[0] + stepx;
            int stepy = step[1], y0 = (axis == 1) ? ldi[1] : tri[1], y1 = ldi[1] + stepy;
            int stepz = step[2], z0 = (axis == 2) ? ldi[2] : tri[2], z1 = ldi[2] + stepz;

            for (int x = x0; x != x1; x += stepx) {
                for (int z = z0; z != z1; z += stepz) {
                    for (int y = y0; y != y1; y += stepy) {
                        BlockState block = this.level.getBlockState(pos.set(x, y, z));
                        if (!block.isPathfindable(PathComputationType.LAND)) return false;
                    }
                }
            }
        } while (t <= max_t);

        return true;
    }

    private static int   leadEdgeToInt(float coord, int step)  { return Mth.floor(coord - step * EPSILON); }
    private static int   trailEdgeToInt(float coord, int step) { return Mth.floor(coord + step * EPSILON); }
    private static float element(Vec3 v, int i) {
        return switch (i) { case 0 -> (float) v.x; case 1 -> (float) v.y; default -> (float) v.z; };
    }
}
