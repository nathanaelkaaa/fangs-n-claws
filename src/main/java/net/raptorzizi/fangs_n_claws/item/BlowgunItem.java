package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.dart_goblin.PoisonousDartEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import java.util.List;

public class BlowgunItem extends Item {

    public static final int CHARGE_TICKS = 20;
    private static final float MAX_VELOCITY = 2.2F;
    private static final float MIN_CHARGE = 0.2F;

    public BlowgunItem() {
        super(new Properties().stacksTo(1));
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int    chargedTicks   = this.getUseDuration(stack, entity) - timeCharged;
        float  chargePercent  = Math.min(chargedTicks / (float) CHARGE_TICKS, 1.0f);
        if (chargePercent < MIN_CHARGE) return;

        if (!level.isClientSide) {
            ItemStack dartStack = findDarts(player);
            if (dartStack.isEmpty() && !player.getAbilities().instabuild) return;

            PoisonousDartEntity dart = new PoisonousDartEntity(EntityRegistry.POISONOUS_DART.get(), level);
            dart.setOwner(player);
            dart.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            dart.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, chargePercent * MAX_VELOCITY, 1.0F);
            level.addFreshEntity(dart);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundsRegistry.BLOWGUN_SHOOT.get(), SoundSource.PLAYERS,
                    0.8F, 1.4F + level.random.nextFloat() * 0.2F);

            if (!player.getAbilities().instabuild) {
                dartStack.shrink(1);
            }
        }
    }

    // Animation

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // Tooltip

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.blowgun.tooltip1"));
    }

    // Helpers

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
