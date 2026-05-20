package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.raptorzizi.fangs_n_claws.entity.werewolf.WerewolfEntity;

import java.util.List;

public class SilverSwordItem extends SwordItem {

    public static final float WEREWOLF_BONUS_DAMAGE = 2.0f;

    public SilverSwordItem() {
        super(Tiers.IRON, new Properties()
                .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4f)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.silver_sword.tooltip1"));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide()
                && target instanceof WerewolfEntity
                && attacker instanceof Player player) {
            target.invulnerableTime = 0;
            target.hurt(attacker.damageSources().playerAttack(player), WEREWOLF_BONUS_DAMAGE);
        }
        return result;
    }
}
