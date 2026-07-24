package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SilverSwordItem extends SwordItem {

    private static final float MAGIC_BONUS_DAMAGE = 2.0f;

    public SilverSwordItem() {
        super(Tiers.IRON, 1, -2.4f, new Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        if (!level.isClientSide) {
            DamageSource magic = target.damageSources().indirectMagic(attacker, null);
            target.invulnerableTime = 0;
            target.hurt(magic, MAGIC_BONUS_DAMAGE);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.silver_sword.tooltip1"));
        lines.add(Component.translatable("item.fangs_n_claws.silver_sword.tooltip2"));
    }
}
