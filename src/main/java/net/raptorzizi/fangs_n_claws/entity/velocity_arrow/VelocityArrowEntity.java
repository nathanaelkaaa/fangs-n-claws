package net.raptorzizi.fangs_n_claws.entity.velocity_arrow;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import org.jetbrains.annotations.Nullable;

public class VelocityArrowEntity extends AbstractArrow {

    private static final double LAUNCH_FORCE = 3.0;

    public VelocityArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public VelocityArrowEntity(Level level, LivingEntity shooter,
                               ItemStack pickupStack, @Nullable ItemStack weapon) {
        super(EntityRegistry.VELOCITY_ARROW_ENTITY.get(), shooter, level, pickupStack, weapon);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity * 1.2f, inaccuracy);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Vec3 dir = this.getDeltaMovement().normalize();
        result.getEntity().setDeltaMovement(dir.scale(LAUNCH_FORCE));
        result.getEntity().hasImpulse = true;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemsRegistry.VELOCITY_ARROW.get());
    }
}
