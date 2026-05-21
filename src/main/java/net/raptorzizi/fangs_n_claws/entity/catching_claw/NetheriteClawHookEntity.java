package net.raptorzizi.fangs_n_claws.entity.catching_claw;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class NetheriteClawHookEntity extends CatchingClawHookEntity {

    public NetheriteClawHookEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public NetheriteClawHookEntity(Player owner, Level level) {
        super(EntityRegistry.NETHERITE_CLAW_HOOK.get(), owner, level);
    }
}
