package net.raptorzizi.fangs_n_claws.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.fire_pitchfork.FirePitchforkItemRenderer;
import net.raptorzizi.fangs_n_claws.registries.EnchantmentsRegistry;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class FirePitchforkItem extends TridentItem {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public FirePitchforkItem() {
        super(new Properties().durability(128));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID, "Weapon modifier", 5.0, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED,  new AttributeModifier(ATTACK_SPEED_UUID,  "Weapon modifier", -2.9, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.fire_pitchfork.tooltip1"));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.IMPALING
                || enchantment == Enchantments.RIPTIDE
                || enchantment == Enchantments.CHANNELING) {
            return false;
        }
        return enchantment == Enchantments.LOYALTY
            || enchantment == Enchantments.FIRE_ASPECT
            || enchantment == Enchantments.MENDING
            || enchantment == Enchantments.UNBREAKING
            || enchantment == Enchantments.VANISHING_CURSE
            || enchantment == EnchantmentsRegistry.BLAZING.get();
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int chargedTicks = this.getUseDuration(stack) - timeCharged;
        if (chargedTicks < 10) return;
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) return;

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));

            FirePitchforkEntity thrown = new FirePitchforkEntity(
                    EntityRegistry.FIRE_PITCHFORK_ENTITY.get(), level, player, stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

            if (player.getAbilities().instabuild) {
                thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(thrown);
            level.playSound(null, thrown.getX(), thrown.getY(), thrown.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    protected void applyFlamebrand(LivingEntity target) {
        FlamebrandEffect.addFlamebrandStack(target);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyFlamebrand(target);
        int blazingLevel = EnchantmentsRegistry.getLevel(stack, EnchantmentsRegistry.BLAZING);
        if (blazingLevel > 0 && target.isOnFire()) {
            target.invulnerableTime = 0;
            target.hurt(target.level().damageSources().magic(), blazingLevel * 0.5f);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private FirePitchforkItemRenderer renderer;

            @Override
            public FirePitchforkItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.renderer = new FirePitchforkItemRenderer(
                            mc.getBlockEntityRenderDispatcher(),
                            mc.getEntityModels());
                }
                return this.renderer;
            }
        });
    }
}
