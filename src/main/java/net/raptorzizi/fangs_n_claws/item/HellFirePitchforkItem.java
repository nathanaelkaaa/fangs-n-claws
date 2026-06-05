package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.effect.HellFlamebrandEffect;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.HellFirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class HellFirePitchforkItem extends FirePitchforkItem {

    public HellFirePitchforkItem() {
        super(new Properties()
                .durability(256)
                .attributes(HellFirePitchforkItem.createAttributes()));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 7.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.fangs_n_claws.hellfire_pitchfork.tooltip1"));
    }

    @Override
    protected void applyFlamebrand(LivingEntity target) {
        HellFlamebrandEffect.addHellFlamebrandStack(target);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int chargedTicks = this.getUseDuration(stack, entity) - timeCharged;
        if (chargedTicks < 10) return;
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) return;

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            HellFirePitchforkEntity thrown = new HellFirePitchforkEntity(
                    EntityRegistry.HELLFIRE_PITCHFORK_ENTITY.get(), level, player, stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

            if (player.hasInfiniteMaterials()) {
                thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(thrown);

            var soundHolder = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                    .orElse(net.minecraft.sounds.SoundEvents.TRIDENT_THROW);
            level.playSound(null, thrown, soundHolder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.hasInfiniteMaterials()) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        HellFirePitchforkEntity thrown = new HellFirePitchforkEntity(
                EntityRegistry.HELLFIRE_PITCHFORK_ENTITY.get(),
                level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        thrown.pickup = AbstractArrow.Pickup.ALLOWED;
        return thrown;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }
}
