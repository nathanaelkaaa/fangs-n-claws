package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class FirePitchforkItem extends TridentItem {

    protected FirePitchforkItem(Properties properties) {
        super(properties);
    }

    public FirePitchforkItem() {
        this(new Properties()
                .durability(128)
                .attributes(FirePitchforkItem.createAttributes()));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)4.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-2.9F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.fire_pitchfork.tooltip1"));
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.IMPALING)
                || enchantment.is(Enchantments.RIPTIDE)
                || enchantment.is(Enchantments.CHANNELING)) {
            return false;
        }
        return enchantment.value().isSupportedItem(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int chargedTicks = this.getUseDuration(stack, entity) - timeCharged;
        if (chargedTicks < 10) return;

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) return;

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            FirePitchforkEntity thrown = new FirePitchforkEntity(
                    EntityRegistry.FIRE_PITCHFORk_ENTITY.get(), level, player, stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

            if (player.hasInfiniteMaterials()) {
                thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(thrown);

            var soundHolder = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                    .orElse(SoundEvents.TRIDENT_THROW);
            level.playSound(null, thrown, soundHolder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.hasInfiniteMaterials()) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    protected void applyFlamebrand(LivingEntity target, LivingEntity attacker) {
        FlamebrandEffect.addFlamebrandStack(target, attacker);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyFlamebrand(target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        FirePitchforkEntity thrown = new FirePitchforkEntity(
                EntityRegistry.FIRE_PITCHFORk_ENTITY.get(),
                level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        thrown.pickup = AbstractArrow.Pickup.ALLOWED;
        return thrown;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 14;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.supportsEnchantment(stack, enchantment);
    }
}
