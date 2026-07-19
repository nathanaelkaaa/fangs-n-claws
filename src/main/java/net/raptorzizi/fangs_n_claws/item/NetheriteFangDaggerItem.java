package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.registries.EnchantmentsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class NetheriteFangDaggerItem extends SwordItem {

    private static final int BACKSTAB_COOLDOWN_TICKS = 120;

    public NetheriteFangDaggerItem() {
        super(Tiers.NETHERITE, -1, -1.2f, new Properties().fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.netherite_dagger.tooltip1"));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction != ToolActions.SWORD_SWEEP && super.canPerformAction(stack, toolAction);
    }

    @Override
    public AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {
        return new AABB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING
            || enchantment == Enchantments.MENDING
            || enchantment == Enchantments.VANISHING_CURSE
            || enchantment == EnchantmentsRegistry.CRITICAL_BACKSTAB.get()
            || enchantment == EnchantmentsRegistry.QUICK_KILLER.get();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            if (FangDaggerItem.isBackstab(player, target) && !player.getCooldowns().isOnCooldown(stack.getItem())) {

                int qkLevel = EnchantmentsRegistry.getLevel(stack, EnchantmentsRegistry.QUICK_KILLER);
                float cooldownMult = qkLevel == 1 ? 0.75f : qkLevel == 2 ? 0.50f : 1.0f;
                int cooldown = Math.round(BACKSTAB_COOLDOWN_TICKS * cooldownMult);

                target.addEffect(new MobEffectInstance(
                        MobEffectsRegistry.BLEEDING.get(), 200, 1, false, false));
                if (player instanceof ServerPlayer sp) {
                    FncAdvancements.grant(sp, "hunt/root");
                }
                player.getCooldowns().addCooldown(stack.getItem(), cooldown);
                target.level().playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        SoundsRegistry.BACKSTAB_IMPACT.get(), SoundSource.PLAYERS,
                        1.0F, 0.9F + target.level().random.nextFloat() * 0.2F);

                int critLevel = EnchantmentsRegistry.getLevel(stack, EnchantmentsRegistry.CRITICAL_BACKSTAB);
                if (critLevel > 0) {
                    float base  = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float extra = base * (critLevel * 0.5f);
                    target.invulnerableTime = 0;
                    target.hurt(target.level().damageSources().playerAttack(player), extra);
                    target.level().playSound(null,
                            target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 1.0F);
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                target.getX(),
                                target.getY() + target.getBbHeight() * 0.5,
                                target.getZ(),
                                8, 0.3, 0.3, 0.3, 0.1);
                    }
                }

                if (qkLevel > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, qkLevel - 1));
                }
            }
        }
        return result;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}
