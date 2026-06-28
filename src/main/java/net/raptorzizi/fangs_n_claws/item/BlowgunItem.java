package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.projectile.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import java.util.List;

public class BlowgunItem extends Item {

    public static final int CHARGE_TICKS = 20;
    private static final float MAX_VELOCITY = 2.2F;
    private static final float MIN_CHARGE   = 0.2F;

    private boolean startSoundPlayed = false;

    public BlowgunItem() {
        super(new Properties().durability(64));
    }

    private static int enchLevel(ItemStack stack, Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(h -> stack.getEnchantmentLevel(h))
                .orElse(0);
    }

    public static int getChargeDuration(ItemStack stack, Level level) {
        int qc = enchLevel(stack, level, Enchantments.QUICK_CHARGE);
        return Math.max(4, CHARGE_TICKS - 5 * qc);
    }

    // Use

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hasDarts(player) || player.getAbilities().instabuild) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!level.isClientSide) {
            int   chargeDuration = getChargeDuration(stack, level);
            int   elapsed        = getUseDuration(stack, entity) - remainingUseTicks;
            float progress       = chargeDuration > 0 ? (float) elapsed / chargeDuration : 1.0f;

            if (progress < 0.2F) {
                startSoundPlayed = false;
            }
            if (progress >= 0.2F && !startSoundPlayed) {
                startSoundPlayed = true;
                int qc = enchLevel(stack, level, Enchantments.QUICK_CHARGE);
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        getStartSound(qc), SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    private static Holder<SoundEvent> getStartSound(int quickChargeLevel) {
        return switch (quickChargeLevel) {
            case 1 -> SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            case 2 -> SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            case 3 -> SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            default -> SoundEvents.CROSSBOW_LOADING_START;
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int   chargeDuration = getChargeDuration(stack, level);
        int   elapsed        = getUseDuration(stack, entity) - timeCharged;
        float chargePercent  = chargeDuration > 0 ? Math.min((float) elapsed / chargeDuration, 1.0f) : 1.0f;
        if (chargePercent < MIN_CHARGE) return;

        if (!level.isClientSide) {
            ItemStack dartStack = findDarts(player);
            if (dartStack.isEmpty() && !player.getAbilities().instabuild) return;

            boolean multishot  = enchLevel(stack, level, Enchantments.MULTISHOT) > 0;
            float   velocity   = chargePercent * MAX_VELOCITY;
            int     count      = multishot ? 3 : 1;
            float[] yawOffsets = multishot ? new float[]{0.0F, -10.0F, 10.0F} : new float[]{0.0F};

            for (int i = 0; i < count; i++) {
                ItemStack pickupDart = new ItemStack(ItemsRegistry.POISONOUS_DART.get());
                PoisonousDartEntity dart = new PoisonousDartEntity(
                        EntityRegistry.POISONOUS_DART.get(),
                        player.getX(), player.getEyeY() - 0.1, player.getZ(),
                        level, pickupDart, stack);
                dart.setOwner(player);
                if (i > 0) dart.setCreativeOnlyPickup();
                dart.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffsets[i],
                        0.0F, velocity, 1.0F);
                level.addFreshEntity(dart);
            }

            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundsRegistry.BLOWGUN_SHOOT.get(), SoundSource.PLAYERS,
                    0.8F, 1.4F + level.random.nextFloat() * 0.2F);

            if (!player.getAbilities().instabuild) {
                dartStack.shrink(1);
            }
        }
    }

    // Duration / animation

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.blowgun.tooltip1"));
    }
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 1;
    }

    private boolean hasDarts(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ItemsRegistry.POISONOUS_DART.get())) return true;
        }
        return false;
    }

    private ItemStack findDarts(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ItemsRegistry.POISONOUS_DART.get())) return s;
        }
        return ItemStack.EMPTY;
    }
}
