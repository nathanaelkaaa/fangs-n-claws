package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraft.sounds.SoundSource;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class FangDaggerItem extends SwordItem {

    private static final double BACKSTAB_DOT_THRESHOLD = -0.766;
    private static final int    BACKSTAB_COOLDOWN_TICKS = 120;

    public FangDaggerItem() {
        super(Tiers.STONE, 2, -0.8f, new Properties());
    }

    public static boolean isBackstab(LivingEntity attacker, LivingEntity target) {
        Vec3 targetEyeLook = Vec3.directionFromRotation(target.getXRot(), target.yHeadRot);
        Vec3 toAttacker = attacker.position().subtract(target.position());
        if (toAttacker.lengthSqr() < 1e-6) return false;
        return targetEyeLook.dot(toAttacker.normalize()) < BACKSTAB_DOT_THRESHOLD;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.fang_dagger.tooltip1"));
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
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            if (isBackstab(player, target)) {
                // ambient=false, visible=false → suppresses vanilla coloured-orb particles;
                // our BleedingEffect spawns its own blood particles instead.
                target.addEffect(new MobEffectInstance(
                        MobEffectsRegistry.BLEEDING.get(),
                        200, 0, false, false));
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
