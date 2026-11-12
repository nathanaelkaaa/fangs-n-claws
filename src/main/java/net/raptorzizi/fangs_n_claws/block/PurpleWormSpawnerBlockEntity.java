package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.purple_worm.PurpleWormEntity;
import net.raptorzizi.fangs_n_claws.registries.BlockEntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class PurpleWormSpawnerBlockEntity extends BlockEntity {

    /**
     * Le declenchement automatique ignore la creation et le spectateur. Attention au booleen de
     * getNearestPlayer : creativePlayers a VRAI filtre sur NO_CREATIVE_OR_SPECTATOR, a faux il ne
     * retire que les spectateurs. Le nom dit l'inverse de ce qu'il fait (EntityGetter:108).
     * En creation, on reveille le ver a la main par un clic droit sur le bloc.
     */
    // Trigger
    private static final double TRIGGER_RANGE = 25.0;
    private static final int POLL_INTERVAL = 20;
    private static final double EXISTING_WORM_RANGE = 96.0;

    private int countdown = POLL_INTERVAL;

    public PurpleWormSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PURPLE_WORM_SPAWNER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  PurpleWormSpawnerBlockEntity spawner) {
        if (--spawner.countdown > 0) return;
        spawner.countdown = POLL_INTERVAL;

        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 centre = pos.getCenter();
        if (serverLevel.getNearestPlayer(centre.x, centre.y, centre.z, TRIGGER_RANGE, true) == null) {
            return;
        }

        if (wormAlreadyNearby(serverLevel, pos)) return;

        spawn(serverLevel, pos);
    }

    /**
     * Pose le ver et retire le bloc. Le bloc part en dernier, parce que cela detruit ce block
     * entity.
     */
    public static boolean spawn(ServerLevel level, BlockPos pos) {
        PurpleWormEntity worm = EntityRegistry.PURPLE_WORM.get().create(level);
        if (worm == null) return false;

        Vec3 centre = pos.getCenter();
        worm.moveTo(centre.x, pos.getY(), centre.z,
                Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F);
        worm.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
        worm.setPersistenceRequired();
        level.addFreshEntity(worm);

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    private static boolean wormAlreadyNearby(ServerLevel level, BlockPos pos) {
        AABB area = AABB.ofSize(pos.getCenter(),
                EXISTING_WORM_RANGE, EXISTING_WORM_RANGE, EXISTING_WORM_RANGE);
        return !level.getEntitiesOfClass(PurpleWormEntity.class, area).isEmpty();
    }
}
