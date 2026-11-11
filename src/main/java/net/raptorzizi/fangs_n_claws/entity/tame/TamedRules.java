package net.raptorzizi.fangs_n_claws.entity.tame;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
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

    public static final TagKey<EntityType<?>> GUARDIANS =
            TagKey.create(Registries.ENTITY_TYPE, FangsClawsMod.id("tame_guardian"));

    private static boolean isGuardian(@Nullable Entity entity) {
        return entity != null && entity.getType().is(GUARDIANS);
    }

    public static boolean isGuardianTruce(@Nullable Entity attacker, @Nullable Entity victim) {
        if (isGuardian(attacker) && isTamed(victim)) return !truceIsBroken(victim, attacker);
        if (isTamed(attacker) && isGuardian(victim)) return !truceIsBroken(attacker, victim);
        return false;
    }

    private static boolean truceIsBroken(Entity pet, Entity guardian) {
        if (pet instanceof LivingEntity living
                && (living.getLastHurtByMob() == guardian || living.getLastHurtMob() == guardian)) {
            return true;
        }
        if (guardian instanceof Mob guardianMob && guardianMob.getTarget() == pet) return true;

        LivingEntity owner = pet instanceof OwnableEntity owned ? owned.getOwner() : null;
        if (owner == null) return false;

        return owner.getLastHurtMob() == guardian
                || owner.getLastHurtByMob() == guardian
                || (guardian instanceof Mob guardianMob2 && guardianMob2.getTarget() == owner);
    }

    public static boolean allowsDespawn(TamableCreature creature) {
        return !creature.isTamed();
    }

    public static boolean preventsPlayerRest(TamableCreature creature) {
        return !creature.isTamed();
    }
}
