package net.raptorzizi.fangs_n_claws.entity.wild_wolf;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WildWolfBreedGoal extends Goal {

    private static final double SEARCH_RADIUS = 8.0;
    private static final double BREED_DISTANCE = 3.0;
    private static final double SPEED = 1.0;
    private static final int    BREED_TICKS = 60;
    private static final int    XP_MIN = 1, XP_MAX = 7;

    private final WildWolfEntity wolf;
    private WildWolfEntity partner;
    private int breedTick;

    public WildWolfBreedGoal(WildWolfEntity wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private WildWolfEntity findPartner() {
        List<WildWolfEntity> nearby = wolf.level().getEntitiesOfClass(WildWolfEntity.class,
                wolf.getBoundingBox().inflate(SEARCH_RADIUS),
                other -> other != wolf && other.isAlive()
                        && other.getType() == wolf.getType()
                        && other.isInLove());
        return nearby.stream().min(Comparator.comparingDouble(wolf::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (!wolf.isInLove()) return false;
        this.partner = findPartner();
        return partner != null;
    }

    @Override
    public boolean canContinueToUse() {
        return wolf.isInLove() && partner != null && partner.isAlive() && partner.isInLove();
    }

    @Override
    public void start() {
        this.breedTick = 0;
    }

    @Override
    public void stop() {
        this.partner = null;
        this.breedTick = 0;
        wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        wolf.getLookControl().setLookAt(partner, 10.0F, wolf.getMaxHeadXRot());
        wolf.getNavigation().moveTo(partner, SPEED);

        if (wolf.distanceToSqr(partner) > BREED_DISTANCE * BREED_DISTANCE) {
            breedTick = 0;
            return;
        }
        if (++breedTick >= BREED_TICKS) breed();
    }

    private void breed() {
        if (!(wolf.level() instanceof ServerLevel server)) return;

        BabyWildWolfEntity baby = wolf.babyType().create(server);
        if (baby != null) {
            baby.moveTo(wolf.getX(), wolf.getY(), wolf.getZ(), 0.0F, 0.0F);
            baby.finalizeSpawn(server, server.getCurrentDifficultyAt(baby.blockPosition()),
                    MobSpawnType.BREEDING, null);
            baby.setVariant(wolf.getVariant());
            if (wolf.getOwnerUUID() != null) baby.setTame(true, true);
            if (wolf.getOwnerUUID() != null) baby.setOwnerUUID(wolf.getOwnerUUID());
            server.addFreshEntity(baby);
            server.addFreshEntity(new ExperienceOrb(server, wolf.getX(), wolf.getY(), wolf.getZ(),
                    wolf.getRandom().nextInt(XP_MAX - XP_MIN + 1) + XP_MIN));
        }

        wolf.clearLove();
        partner.clearLove();
        wolf.setBreedCooldown();
        partner.setBreedCooldown();
        stop();
    }
}
