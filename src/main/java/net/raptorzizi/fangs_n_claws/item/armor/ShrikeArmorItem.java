package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class ShrikeArmorItem extends ArmorItem implements GeoItem {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) { return 0; }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return switch (type) {
                case HELMET     -> 3;
                case CHESTPLATE -> 8;
                case LEGGINGS   -> 6;
                case BOOTS      -> 3;
            };
        }

        @Override
        public int getEnchantmentValue() { return 10; }

        @Override
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_DIAMOND; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(ItemsRegistry.GIANT_FEATHER.get()); }

        @Override
        public String getName() { return "shrike"; }

        @Override
        public float getToughness() { return 2f; }

        @Override
        public float getKnockbackResistance() { return 0f; }
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShrikeArmorItem(ArmorItem.Type type, int durability) {
        super(MATERIAL, type, new Item.Properties().durability(durability));
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
                stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
            }
        }
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ShrikeArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new ShrikeArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
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
