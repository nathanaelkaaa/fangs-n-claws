package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.tomahawk.TomahawkProjectile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TomahawkItem extends AxeItem {

    private static final int   COOLDOWN_TICKS = 100;
    private static final float THROW_DAMAGE   = 6.0f;
    private static final float THROW_VELOCITY = 1.6f;

    public TomahawkItem() {
        super(Tiers.IRON, 5.0F, -3.1F, new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item tomahawkItem = stack.getItem();
        if (player.getCooldowns().isOnCooldown(tomahawkItem)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            ItemStack thrown = stack.copy();
            thrown.setCount(1);

            TomahawkProjectile tomahawk = new TomahawkProjectile(level, player, thrown);
            tomahawk.setDamage(THROW_DAMAGE);
            tomahawk.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, THROW_VELOCITY, 1.0F);
            level.addFreshEntity(tomahawk);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        player.getCooldowns().addCooldown(tomahawkItem, COOLDOWN_TICKS);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.fangs_n_claws.tomahawk.tooltip1"));
        super.appendHoverText(stack, level, lines, flag);
    }
}
