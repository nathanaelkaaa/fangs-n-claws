package net.raptorzizi.fangs_n_claws.entity.undead_horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkeletonHorseMob extends UndeadHorseMob {

    private static final ResourceLocation TEXTURE = FangsClawsMod.id("textures/entity/skeleton_horse.png");
    private static final int TRAP_MAX_LIFE = 18000;

    private final SkeletonHorseTrapMobGoal trapGoal = new SkeletonHorseTrapMobGoal(this);
    private boolean isTrap;
    private int     trapTime;

    public SkeletonHorseMob(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public ResourceLocation textureLocation() {
        return TEXTURE;
    }

    @Override
    public boolean isHealingItem(ItemStack stack) {
        return stack.is(Items.BONE);
    }

    @Nullable
    @Override
    protected Mob createRider(ServerLevel level) {
        return level.random.nextBoolean()
                ? EntityType.SKELETON.create(level)
                : EntityRegistry.SILVER_SKELETON.get().create(level);
    }

    // Piège

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isTrap() && this.trapTime++ >= TRAP_MAX_LIFE) {
            this.discard();
        }
    }

    public boolean isTrap() {
        return this.isTrap;
    }

    public void setTrap(boolean trap) {
        if (trap != this.isTrap) {
            this.isTrap = trap;
            if (trap) {
                this.goalSelector.addGoal(1, this.trapGoal);
            } else {
                this.goalSelector.removeGoal(this.trapGoal);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SkeletonTrap", this.isTrap());
        tag.putInt("SkeletonTrapTime", this.trapTime);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTrap(tag.getBoolean("SkeletonTrap"));
        this.trapTime = tag.getInt("SkeletonTrapTime");
    }

    // Sons

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.SKELETON_HORSE_HURT;
    }
}
