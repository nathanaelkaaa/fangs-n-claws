package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;

public class MonsterFollowOwnerGoal<T extends PathfinderMob & OwnedMonster> extends Goal {

    private static final int REPATH = 10;

    private final T mob;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private final float teleportDistance;

    private LivingEntity owner;
    private int repathCooldown;

    public MonsterFollowOwnerGoal(T mob, double speed, float startDistance, float stopDistance,
                                  float teleportDistance) {
        this.mob = mob;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.teleportDistance = teleportDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.isTamed() || mob.isOrderedToSit()) return false;
        LivingEntity candidate = mob.getOwner();
        if (candidate == null || candidate.isSpectator()) return false;
        if (candidate instanceof Player p && p.isSpectator()) return false;
        if (mob.distanceToSqr(candidate) < startDistance * startDistance) return false;
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isTamed() && !mob.isOrderedToSit()
                && owner != null && owner.isAlive()
                && mob.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(owner, 10.0F, mob.getMaxHeadXRot());
        if (--repathCooldown > 0) return;
        repathCooldown = REPATH;

        if (mob.distanceToSqr(owner) >= teleportDistance * teleportDistance) {
            teleportNearOwner();
            return;
        }
        mob.getNavigation().moveTo(owner, speed);
    }

    private void teleportNearOwner() {
        BlockPos target = owner.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = randomOffset(-3, 3);
            int dy = randomOffset(-1, 1);
            int dz = randomOffset(-3, 3);
            if (tryTeleport(target.getX() + dx, target.getY() + dy, target.getZ() + dz)) return;
        }
    }

    private int randomOffset(int min, int max) {
        return mob.getRandom().nextInt(max - min + 1) + min;
    }

    private boolean tryTeleport(int x, int y, int z) {
        if (Math.abs(x - owner.getX()) < 2.0 && Math.abs(z - owner.getZ()) < 2.0) return false;
        if (!canTeleportTo(new BlockPos(x, y, z))) return false;

        mob.moveTo(x + 0.5, y, z + 0.5, mob.getYRot(), mob.getXRot());
        mob.getNavigation().stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNavigation navigation = mob.getNavigation();
        var type = WalkNodeEvaluator.getPathTypeStatic(mob, pos);
        if (type != PathType.WALKABLE) return false;
        return mob.level().noCollision(mob, mob.getBoundingBox().move(
                pos.getX() - mob.getX(), pos.getY() - mob.getY(), pos.getZ() - mob.getZ()))
                && navigation != null;
    }
}
