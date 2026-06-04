package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.NetheriteClawHookEntity;

import java.util.List;
import java.util.UUID;

public class NetheriteClawItem extends Item {

    private static final float THROW_VELOCITY   = 1.8f;
    private static final float THROW_INACCURACY = 0.8f;

    private static ItemAttributeModifiers buildAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                4.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_ID,
                                -2.8,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public NetheriteClawItem() {
        super(new Properties().stacksTo(1).durability(256).fireResistant().attributes(buildAttributes()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID("HookUUID") ? tag.getUUID("HookUUID") : null;
    }

    private static void setHookUUID(ItemStack stack, UUID uuid) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (uuid != null) tag.putUUID("HookUUID", uuid);
        else              tag.remove("HookUUID");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }
}
