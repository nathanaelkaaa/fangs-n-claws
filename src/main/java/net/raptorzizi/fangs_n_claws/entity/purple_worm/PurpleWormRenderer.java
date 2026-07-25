package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.network.PurpleWormPartsPayload;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

public class PurpleWormRenderer extends GeoEntityRenderer<PurpleWormEntity> {

    private static final ResourceLocation EYES =
            FangsClawsMod.id("textures/entity/glowing_eyes/purple_worm_eyes.png");

    public PurpleWormRenderer(EntityRendererProvider.Context context) {
        super(context, new PurpleWormModel());
        this.shadowRadius = 1.6f;
        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, PurpleWormEntity animatable, BakedGeoModel bakedModel,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType eyesType = RenderType.eyes(EYES);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType,
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay, -1);
            }
        });
    }

    @Override
    public void render(PurpleWormEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        for (String name : PurpleWormEntity.PART_BONES) {
            this.getGeoModel().getBone(name).ifPresent(b -> b.setTrackingMatrices(true));
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        syncPartOffsets(entity);
    }

    private void syncPartOffsets(PurpleWormEntity entity) {
        if (entity.getLastPartSyncTick() == entity.tickCount) return;
        entity.setLastPartSyncTick(entity.tickCount);

        float[] offsets = new float[PurpleWormEntity.PART_COUNT * 3];
        Vec3 pos = entity.position();
        for (int i = 0; i < PurpleWormEntity.PART_BONES.length; i++) {
            Optional<GeoBone> bone = this.getGeoModel().getBone(PurpleWormEntity.PART_BONES[i]);
            if (bone.isEmpty()) return;
            Vector3d wp = bone.get().getWorldPosition();
            offsets[i * 3]     = (float) (wp.x - pos.x);
            offsets[i * 3 + 1] = (float) (wp.y - pos.y);
            offsets[i * 3 + 2] = (float) (wp.z - pos.z);
        }

        Vec3[] local = new Vec3[PurpleWormEntity.PART_COUNT];
        for (int i = 0; i < local.length; i++) {
            local[i] = new Vec3(offsets[i * 3], offsets[i * 3 + 1], offsets[i * 3 + 2]);
        }
        entity.setAnimatedPartOffsets(local);
        PacketDistributor.sendToServer(new PurpleWormPartsPayload(entity.getId(), offsets));
    }

    @Override
    protected float getDeathMaxRotation(PurpleWormEntity animatable) {
        return 0f;
    }
}
