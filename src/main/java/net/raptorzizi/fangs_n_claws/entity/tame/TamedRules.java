package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.IronGolem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class TamedRules {

    private TamedRules() { }

    public static boolean isTamed(@Nullable Entity entity) {
        return entity instanceof TamableCreature creature && creature.isTamed();
    }

    @Nullable
    public static UUID ownerOf(@Nullable Entity entity) {
        return entity instanceof OwnableEntity owned ? owned.getOwnerUUID() : null;
    }

    public static boolean isFriendlyFire(@Nullable Entity attacker, @Nullable Entity victim) {
        if (attacker == null || victim == null || attacker == victim) return false;

        UUID owner = ownerOf(attacker);
        if (owner == null) return false;

        if (owner.equals(victim.getUUID())) return true;
        return owner.equals(ownerOf(victim));
    }

    public static boolean isGuardianTruce(@Nullable Entity attacker, @Nullable Entity victim) {
        return (attacker instanceof IronGolem && isTamed(victim))
                || (isTamed(attacker) && victim instanceof IronGolem);
    }

    public static boolean allowsDespawn(TamableCreature creature) {
        return !creature.isTamed();
    }

    public static boolean preventsPlayerRest(TamableCreature creature) {
        return !creature.isTamed();
    }
}
