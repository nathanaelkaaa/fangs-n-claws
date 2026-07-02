package net.raptorzizi.fangs_n_claws.item.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

public class OwlArmorItem extends ArmorItem implements GeoItem {

    public static final Holder<ArmorMaterial> MATERIAL = Holder.direct(new ArmorMaterial(
            Map.of(
                    Type.HELMET,     2,
                    Type.CHESTPLATE, 6,
                    Type.LEGGINGS,   5,
                    Type.BOOTS,      2
            ),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(ItemsRegistry.GIANT_FEATHER.get()),
            List.of(new ArmorMaterial.Layer(FangsClawsMod.id("owl_armor"))),
            0f,
            0f
    ));

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public OwlArmorItem(Type type, int durability) {
        super(MATERIAL, type, new Item.Properties().durability(durability));
    }

    public static boolean hasOwlChestplate(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof OwlArmorItem;
    }

    public static int countOwlPieces(LivingEntity entity) {
        int count = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem()  instanceof OwlArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof OwlArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem()  instanceof OwlArmorItem) count++;
        if (entity.getItemBySlot(EquipmentSlot.FEET).getItem()  instanceof OwlArmorItem) count++;
        return count;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private OwlArmorRenderer renderer;

            @Override
            @Nullable
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity, ItemStack itemStack,
                    @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new OwlArmorRenderer();
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
