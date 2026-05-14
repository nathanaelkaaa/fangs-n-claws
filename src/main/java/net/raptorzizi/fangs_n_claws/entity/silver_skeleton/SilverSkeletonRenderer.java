package net.raptorzizi.fangs_n_claws.entity.silver_skeleton;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class SilverSkeletonRenderer extends GeoEntityRenderer<SilverSkeletonEntity> {

    public SilverSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverSkeletonModel());

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {

            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, SilverSkeletonEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem" -> animatable.getItemBySlot(EquipmentSlot.MAINHAND);
                    case "leftItem"  -> animatable.getItemBySlot(EquipmentSlot.OFFHAND);
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, SilverSkeletonEntity animatable) {
                return switch (bone.getName()) {
                    case "rightItem" -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    case "leftItem"  -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }
        });
    }
}
