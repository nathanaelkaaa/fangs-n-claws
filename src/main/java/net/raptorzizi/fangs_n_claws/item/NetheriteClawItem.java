package net.raptorzizi.fangs_n_claws.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookEntity;

import java.util.List;
import java.util.UUID;

public class NetheriteClawItem extends Item {

    private static final float THROW_VELOCITY   = 1.8f;
    private static final float THROW_INACCURACY = 0.8f;

    private static final double BASE_ATTACK_DAMAGE = 3.0;
    private static final double BASE_ATTACK_SPEED  = -2.8;

    // Fixed UUIDs for attribute modifiers
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("6DEF7A8B-C9D0-1E2F-3456-789ABCDEF012");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("B0C1D2E3-F4A5-6789-BCDE-F01234567891");

    public NetheriteClawItem() {
        super(new Properties().stacksTo(1).durability(256).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            int scratch = CatchingClawItem.getScratchLevel(stack);
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID, "Weapon modifier", BASE_ATTACK_DAMAGE + scratch * 0.25, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED,  new AttributeModifier(ATTACK_SPEED_UUID,  "Weapon modifier", BASE_ATTACK_SPEED, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("item.fangs_n_claws.catching_claw.tooltip1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide) {
            UUID existingUUID = getHookUUID(stack);

            if (existingUUID != null && level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(existingUUID);
                if (entity instanceof NetheriteClawHookEntity hook && hook.isAlive()) {
                    hook.retrieve(player);
                }
                setHookUUID(stack, null);
                return InteractionResultHolder.success(stack);
            }

            NetheriteClawHookEntity hook = new NetheriteClawHookEntity(player, level);
            hook.shootFromRotation(player,
                    player.getXRot(), player.getYRot(),
                    0.0f, THROW_VELOCITY, THROW_INACCURACY);
            level.addFreshEntity(hook);
            setHookUUID(stack, hook.getUUID());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static UUID getHookUUID(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        return tag.hasUUID("HookUUID") ? tag.getUUID("HookUUID") : null;
    }

    private static void setHookUUID(ItemStack stack, UUID uuid) {
        CompoundTag tag = stack.getOrCreateTag();
        if (uuid != null) tag.putUUID("HookUUID", uuid);
        else              tag.remove("HookUUID");
        stack.setTag(tag);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}
