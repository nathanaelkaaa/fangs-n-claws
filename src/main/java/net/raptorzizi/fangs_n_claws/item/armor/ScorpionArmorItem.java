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

public class ScorpionArmorItem extends ArmorItem implements GeoItem {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) { return 0; }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return switch (type) {
                case HELMET     -> 2;
                case CHESTPLATE -> 6;
                case LEGGINGS   -> 5;
                case BOOTS      -> 2;
            };
        }

        @Override
        public int getEnchantmentValue() { return 12; }

        @Override
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_CHAIN; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.of(ItemsRegistry.CHITIN.get()); }

        @Override
        public String getName() { return "scorpion"; }

        @Override
        public float getToughness() { return 0.5f; }

        @Override
        public float getKnockbackResistance() { return 0f; }
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ScorpionArmorItem(ArmorItem.Type type, int durability) {
        super(MATERIAL, type, new Item.Properties().durability(durability));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ScorpionArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new ScorpionArmorRenderer();
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
