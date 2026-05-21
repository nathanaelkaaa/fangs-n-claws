package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

public class NetheriteFangDaggerItem extends SwordItem {

    private static final int    BACKSTAB_COOLDOWN_TICKS = 120;

    public NetheriteFangDaggerItem() {
        super(Tiers.NETHERITE, new Properties()
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 0, -0.8f))
                .fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.fang_dagger.tooltip1"));
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, itemAbility);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            if (FangDaggerItem.isBackstab(player, target)) {
                target.addEffect(new MobEffectInstance(
                        MobEffectsRegistry.BLEEDING,
                        200, 1));
                player.getCooldowns().addCooldown(stack.getItem(), BACKSTAB_COOLDOWN_TICKS);
                target.level().playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        SoundsRegistry.BACKSTAB_IMPACT.get(), SoundSource.PLAYERS,
                        1.0F, 0.9F + target.level().random.nextFloat() * 0.2F);
            }
        }
        return result;
    }
}
