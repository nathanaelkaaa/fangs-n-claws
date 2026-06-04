package net.raptorzizi.fangs_n_claws.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.raptorzizi.fangs_n_claws.effect.FlamebrandEffect;
import net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork.DecrepitPitchforkEntity;
import net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork.DecrepitPitchforkItemRenderer;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

import java.util.UUID;
import java.util.function.Consumer;

public class DecrepitPitchforkItem extends TridentItem {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public DecrepitPitchforkItem() {
        super(new Properties().durability(250));
    }

    // 1.20.1: attribute modifiers via Multimap override
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

    // 1.20.1: enchantment compatibility via Forge hook
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.LOYALTY
            || enchantment == Enchantments.FIRE_ASPECT
            || enchantment == Enchantments.MENDING
            || enchantment == Enchantments.UNBREAKING
            || enchantment == Enchantments.VANISHING_CURSE;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    // 1.20.1: single-param getUseDuration
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
            // 1.20.1: hurtAndBreak with lambda
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));

            DecrepitPitchforkEntity thrown = new DecrepitPitchforkEntity(
                    EntityRegistry.DECREPIT_PITCHFORK_ENTITY.get(), level, player, stack);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

            if (player.getAbilities().instabuild) {
                thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(thrown);

            // 1.20.1: plain SoundEvent, no .value()
            level.playSound(null, thrown.getX(), thrown.getY(), thrown.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        FlamebrandEffect.addFlamebrandStack(target);
        return super.hurtEnemy(stack, target, attacker);
    }

    // 1.20.1 BEWLR: override initializeClient instead of RegisterClientExtensionsEvent
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DecrepitPitchforkItemRenderer renderer;

            @Override
            public DecrepitPitchforkItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.renderer = new DecrepitPitchforkItemRenderer(
                            mc.getBlockEntityRenderDispatcher(),
                            mc.getEntityModels());
                }
                return this.renderer;
            }
        });
    }
}
