package net.raptorzizi.fangs_n_claws.entity.undead_horse;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.Nullable;

public class SkeletonHorseTrapMobGoal extends Goal {

    private final SkeletonHorseMob horse;

    public SkeletonHorseTrapMobGoal(SkeletonHorseMob horse) {
        this.horse = horse;
    }

    @Override
    public boolean canUse() {
        return this.horse.level().hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) this.horse.level();
        level.getServer().tell(new TickTask(level.getServer().getTickCount(), () -> {
            if (!this.horse.isAlive()) return;
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(this.horse.blockPosition());
            this.horse.setTrap(false);
            this.horse.setTamed(true);
            this.horse.setAge(0);

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) return;
            bolt.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);

            Skeleton rider = this.createSkeleton(difficulty, this.horse);
            if (rider == null) return;
            rider.startRiding(this.horse);
            level.addFreshEntityWithPassengers(rider);

            for (int i = 0; i < 3; i++) {
                SkeletonHorseMob extra = this.createHorse(difficulty);
                if (extra == null) continue;
                Skeleton extraRider = this.createSkeleton(difficulty, extra);
                if (extraRider == null) continue;
                extraRider.startRiding(extra);
                extra.push(this.horse.getRandom().triangle(0.0, 1.1485), 0.0, this.horse.getRandom().triangle(0.0, 1.1485));
                level.addFreshEntityWithPassengers(extra);
            }
        }));
    }

    @Nullable
    private SkeletonHorseMob createHorse(DifficultyInstance difficulty) {
        SkeletonHorseMob mount = EntityRegistry.SKELETON_HORSE_MOB.get().create(this.horse.level());
        if (mount != null) {
            mount.finalizeSpawn((ServerLevel) this.horse.level(), difficulty, MobSpawnType.TRIGGERED, null, null);
            mount.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            mount.invulnerableTime = 60;
            mount.setPersistenceRequired();
            mount.setTamed(true);
            mount.setAge(0);
        }
        return mount;
    }

    @Nullable
    private Skeleton createSkeleton(DifficultyInstance difficulty, AbstractHorse mount) {
        Skeleton skeleton = EntityType.SKELETON.create(mount.level());
        if (skeleton != null) {
            skeleton.finalizeSpawn((ServerLevel) mount.level(), difficulty, MobSpawnType.TRIGGERED, null, null);
            skeleton.setPos(mount.getX(), mount.getY(), mount.getZ());
            skeleton.invulnerableTime = 60;
            skeleton.setPersistenceRequired();
            if (skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
        }
        return skeleton;
    }
}
