package net.raptorzizi.fangs_n_claws.compat;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.util.DimensionSpawnCap;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class BetterMobCombatCompat {

    private static final String BMC_MOD_ID = "better_mob_combat_reimagined";
    private static final String SETTER     = "bmc$setCallingVanillaAttack";

    private static boolean resolved = false;
    private static Method  setter   = null;

    private BetterMobCombatCompat() { }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!DimensionSpawnCap.isOurMob(event.getEntity())) return;

        keepVanillaMelee(event.getEntity());
    }

    private static void keepVanillaMelee(Entity entity) {
        Method method = resolveSetter(entity);
        if (method == null) return;

        try {
            method.invoke(entity, true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            setter = null;
            FangsClawsMod.LOGGER.warn(
                    "Better Mob Combat compat disabled: {} could not be invoked", SETTER, e);
        }
    }

    private static Method resolveSetter(Entity entity) {
        if (resolved) return setter;
        resolved = true;

        if (!ModList.get().isLoaded(BMC_MOD_ID)) return null;

        try {
            setter = entity.getClass().getMethod(SETTER, boolean.class);
            FangsClawsMod.LOGGER.info("Better Mob Combat detected; keeping vanilla melee on {} mobs",
                    FangsClawsMod.MOD_ID);
        } catch (NoSuchMethodException | RuntimeException e) {
            FangsClawsMod.LOGGER.warn(
                    "Better Mob Combat is present but {} was not found; mod mobs may deal no melee "
                            + "damage. Workaround: add \"#{}:no_better_mob_combat\" to entityBlacklist "
                            + "in its server config.",
                    SETTER, FangsClawsMod.MOD_ID);
        }
        return setter;
    }
}
