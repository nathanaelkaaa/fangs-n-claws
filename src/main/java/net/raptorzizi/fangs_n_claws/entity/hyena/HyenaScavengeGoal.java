package net.raptorzizi.fangs_n_claws.entity.hyena;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class HyenaScavengeGoal extends Goal {

    public static final TagKey<Item> HYENA_FOOD =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(
                    FangsClawsMod.MOD_ID, "hyena_food"));

    private static final double SEARCH_RADIUS = 12.0;
    private static final double SEARCH_HEIGHT = 4.0;
    private static final double GRAB_DISTANCE = 1.4;
    private static final double SPEED         = 1.1;
    private static final int    REPATH_PERIOD = 10;

    private final HyenaEntity hyena;
    private ItemEntity food;
    private int repathCooldown;

    public HyenaScavengeGoal(HyenaEntity hyena) {
        this.hyena = hyena;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isEdible(ItemEntity item) {
        if (!item.isAlive() || item.hasPickUpDelay()) return false;
        ItemStack stack = item.getItem();
        return !stack.isEmpty() && stack.is(HYENA_FOOD);
    }

    @Override
    public boolean canUse() {
        if (hyena.getTarget() != null) return false;
        if (!hyena.getMainHandItem().isEmpty()) return false;

        List<ItemEntity> nearby = hyena.level().getEntitiesOfClass(ItemEntity.class,
                hyena.getBoundingBox().inflate(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS),
                this::isEdible);
        if (nearby.isEmpty()) return false;

        this.food = nearby.stream().min(Comparator.comparingDouble(hyena::distanceToSqr)).orElse(null);
        return this.food != null;
    }

    @Override
    public boolean canContinueToUse() {
        return hyena.getTarget() == null && hyena.getMainHandItem().isEmpty()
                && food != null && isEdible(food);
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.food = null;
        hyena.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (food == null) return;
        hyena.getLookControl().setLookAt(food, 30.0F, 30.0F);

        if (hyena.distanceToSqr(food) > GRAB_DISTANCE * GRAB_DISTANCE) {
            if (--repathCooldown <= 0) {
                repathCooldown = REPATH_PERIOD;
                hyena.getNavigation().moveTo(food, SPEED);
            }
            return;
        }

        grab();
    }

    private void grab() {
        ItemStack held = food.getItem().split(1);
        if (food.getItem().isEmpty()) food.discard();

        hyena.setItemSlot(EquipmentSlot.MAINHAND, held);
        hyena.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
        hyena.level().playSound(null, hyena.blockPosition(), SoundEvents.FOX_EAT,
                SoundSource.NEUTRAL, 0.7F, 0.9F + hyena.getRandom().nextFloat() * 0.2F);
        stop();
    }
}
