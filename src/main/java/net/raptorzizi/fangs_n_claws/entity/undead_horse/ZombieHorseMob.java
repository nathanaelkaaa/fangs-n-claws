package net.raptorzizi.fangs_n_claws.entity.undead_horse;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieHorseMob extends UndeadHorseMob {

    private static final ResourceLocation TEXTURE = FangsClawsMod.id("textures/entity/zombie_horse.png");

    public ZombieHorseMob(EntityType<? extends AbstractHorse> type, Level level) {
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
        return stack.is(Items.ROTTEN_FLESH);
    }

    @Nullable
    @Override
    protected Mob createRider(ServerLevel level) {
        return EntityType.ZOMBIE.create(level);
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }
}
