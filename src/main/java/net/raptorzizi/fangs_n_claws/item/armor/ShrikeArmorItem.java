package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShrikeArmorItem extends ArmorItem implements GeoItem {

    public static final Holder<ArmorMaterial> MATERIAL = Holder.direct(new ArmorMaterial(
            Map.of(
                    Type.HELMET,     3,
                    Type.CHESTPLATE, 8,
                    Type.LEGGINGS,   6,
                    Type.BOOTS,      3
            ),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(ItemsRegistry.GIANT_FEATHER.get()),
            List.of(new ArmorMaterial.Layer(FangsClawsMod.id("shrike_armor"))),
            2f,
            0f
    ));

    private static final double JUMP_AMORTI_PER_PIECE = 0.05;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShrikeArmorItem(Type type, int durability) {
        super(MATERIAL, type, new Item.Properties().durability(durability).attributes(buildModifiers(type)));
    }

    private static ItemAttributeModifiers buildModifiers(Type type) {
        ArmorMaterial mat = MATERIAL.value();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(type.getSlot());
        String slot = type.getName();
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();

        int defense = mat.defense().getOrDefault(type, 0);
        b.add(Attributes.ARMOR,
                new AttributeModifier(FangsClawsMod.id("shrike_armor_" + slot), defense, AttributeModifier.Operation.ADD_VALUE), group);
        if (mat.toughness() > 0)
            b.add(Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(FangsClawsMod.id("shrike_toughness_" + slot), mat.toughness(), AttributeModifier.Operation.ADD_VALUE), group);
        if (mat.knockbackResistance() > 0)
            b.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(FangsClawsMod.id("shrike_kb_" + slot), mat.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE), group);

        b.add(Attributes.JUMP_STRENGTH,
                new AttributeModifier(FangsClawsMod.id("shrike_jump_" + slot), JUMP_AMORTI_PER_PIECE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
        b.add(Attributes.SAFE_FALL_DISTANCE,
                new AttributeModifier(FangsClawsMod.id("shrike_safefall_" + slot), JUMP_AMORTI_PER_PIECE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
        b.add(Attributes.FALL_DAMAGE_MULTIPLIER,
                new AttributeModifier(FangsClawsMod.id("shrike_falldmg_" + slot), -JUMP_AMORTI_PER_PIECE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);

        return b.build();
    }

    public static boolean hasShrikeChestplate(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ShrikeArmorItem;
    }

    public static int countShrikePieces(LivingEntity entity) {
        int count = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem()  instanceof ShrikeArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ShrikeArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem()  instanceof ShrikeArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.FEET).getItem()  instanceof ShrikeArmorItem) count++;
        return count;
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return this.getType() == Type.CHESTPLATE;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int next = flightTicks + 1;
            if (next % 20 == 0) {
                stack.hurtAndBreak(1, entity, EquipmentSlot.CHEST);
            }
        }
        return true;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ShrikeArmorRenderer renderer;

            @Override
            @Nullable
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity, ItemStack itemStack,
                    @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new ShrikeArmorRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
