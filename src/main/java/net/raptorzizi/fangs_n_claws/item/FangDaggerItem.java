package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.raptorzizi.fangs_n_claws.advancement.FncAdvancements;
import net.raptorzizi.fangs_n_claws.registries.EnchantmentsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import java.util.List;

public class FangDaggerItem extends SwordItem {

    private static final double BACKSTAB_DOT_THRESHOLD  = -0.766;
    private static final int    BACKSTAB_COOLDOWN_TICKS = 120;

    public FangDaggerItem() {
        super(Tiers.STONE, new Properties()
                .attributes(SwordItem.createAttributes(Tiers.STONE, 1, -1.1f)));
    }

    public static boolean isBackstab(LivingEntity attacker, LivingEntity target) {
        Vec3 targetEyeLook = Vec3.directionFromRotation(target.getXRot(), target.yHeadRot);
        Vec3 toAttacker    = attacker.position().subtract(target.position());
        if (toAttacker.lengthSqr() < 1e-6) return false;
        return targetEyeLook.dot(toAttacker.normalize()) < BACKSTAB_DOT_THRESHOLD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.fang_dagger.tooltip1"));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, itemAbility);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            if (isBackstab(player, target) && !player.getCooldowns().isOnCooldown(stack.getItem())) {

                int qkLevel = EnchantmentsRegistry.getLevel(stack, attacker.level(), EnchantmentsRegistry.QUICK_KILLER);
                float cooldownMultiplier = qkLevel == 1 ? 0.75f : qkLevel == 2 ? 0.50f : 1.0f;
                int cooldown = Math.round(BACKSTAB_COOLDOWN_TICKS * cooldownMultiplier);

                target.addEffect(new MobEffectInstance(MobEffectsRegistry.BLEEDING, 200, 0));
                if (player instanceof ServerPlayer sp) {
                    FncAdvancements.grant(sp, "hunt/root");
                }
                player.getCooldowns().addCooldown(stack.getItem(), cooldown);
                target.level().playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        SoundsRegistry.BACKSTAB_IMPACT.get(), SoundSource.PLAYERS,
                        1.0F, 0.9F + target.level().random.nextFloat() * 0.2F);

                int critLevel = EnchantmentsRegistry.getLevel(stack, attacker.level(), EnchantmentsRegistry.CRITICAL_BACKSTAB);
                if (critLevel > 0) {
                    float base  = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float extra = base * (critLevel * 0.5f);
                    target.invulnerableTime = 0;
                    target.hurt(attacker.damageSources().playerAttack(player), extra);
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
    public int getEnchantmentValue(ItemStack stack) {
        return 14;
    }
}
