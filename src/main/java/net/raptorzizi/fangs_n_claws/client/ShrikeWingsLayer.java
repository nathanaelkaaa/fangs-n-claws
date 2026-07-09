package net.raptorzizi.fangs_n_claws.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.item.armor.ShrikeArmorItem;

public class ShrikeWingsLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE =
            FangsClawsMod.id("textures/entity/shrike_wings.png");

    private static final float FLAP_SWEEP = 1.0f;

    private final ModelPart wings;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public ShrikeWingsLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent);
        this.wings = models.bakeLayer(ModelLayers.ELYTRA);
        this.leftWing  = this.wings.getChild("left_wing");
        this.rightWing = this.wings.getChild("right_wing");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ShrikeArmorItem sa) || sa.getType() != ArmorItem.Type.CHESTPLATE) return;

        poseElytra(entity);

        if (entity instanceof Player player) {
            float sweep = OwlFlightState.flapWingSweep(player) * FLAP_SWEEP;
            if (sweep != 0f) {
                this.leftWing.xRot  += sweep;
                this.rightWing.xRot += sweep;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(TEXTURE), false, chest.hasFoil());
        this.wings.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void poseElytra(T entity) {
        float f  = (float) (Math.PI / 12);
        float f1 = (float) (-Math.PI / 12);
        float f2 = 0.0F;
        float f3 = 0.0F;

        if (entity.isFallFlying()) {
            float f4 = 1.0F;
            Vec3 v = entity.getDeltaMovement();
            if (v.y < 0.0) {
                Vec3 n = v.normalize();
                f4 = 1.0F - (float) Math.pow(-n.y, 1.5);
            }
            f  = f4 * (float) (Math.PI / 9) + (1.0F - f4) * f;
            f1 = f4 * (float) (-Math.PI / 2) + (1.0F - f4) * f1;
        } else if (entity.isCrouching()) {
            f  = (float) (Math.PI * 2.0 / 9.0);
            f1 = (float) (-Math.PI / 4);
            f2 = 3.0F;
            f3 = 0.08726646F;
        }

        this.leftWing.y    = f2;
        this.leftWing.xRot = f;
        this.leftWing.yRot = f3;
        this.leftWing.zRot = f1;

        this.rightWing.y    = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }
}
