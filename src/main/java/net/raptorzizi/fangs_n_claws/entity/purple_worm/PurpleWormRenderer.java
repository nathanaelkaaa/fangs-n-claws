package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.network.FangsNetwork;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.network.PurpleWormPartsPayload;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
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
                        bufferSource.getBuffer(eyesType), partialTick, LightTexture.FULL_SKY, packedOverlay,
                        1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    @Override
    public void render(PurpleWormEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isHidden()) return;

        for (String name : PurpleWormEntity.PART_BONES) {
            this.getGeoModel().getBone(name).ifPresent(b -> b.setTrackingMatrices(true));
        }
        this.getGeoModel().getBone("Lower Jaw").ifPresent(b -> b.setTrackingMatrices(true));
        this.getGeoModel().getBone("Mini Head L").ifPresent(b -> b.setTrackingMatrices(true));
        this.getGeoModel().getBone("Mini Head R").ifPresent(b -> b.setTrackingMatrices(true));

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

        float[] headFwd = computeHeadForward();
        if (headFwd != null) {
            entity.setBreathHeadForward(new Vec3(headFwd[0], headFwd[1], headFwd[2]));
        } else {
            headFwd = new float[3];
        }

        float[] mini = new float[6];
        boneOffset("Mini Head L", pos, mini, 0);
        boneOffset("Mini Head R", pos, mini, 3);
        entity.setMiniHeadOffsets(
                new Vec3(mini[0], mini[1], mini[2]),
                new Vec3(mini[3], mini[4], mini[5]));

        FangsNetwork.CHANNEL.sendToServer(new PurpleWormPartsPayload(entity.getId(), offsets, headFwd, mini));
    }

    private void boneOffset(String bone, Vec3 entityPos, float[] out, int base) {
        Optional<GeoBone> b = this.getGeoModel().getBone(bone);
        if (b.isEmpty()) return;
        Vector3d wp = b.get().getWorldPosition();
        out[base]     = (float) (wp.x - entityPos.x);
        out[base + 1] = (float) (wp.y - entityPos.y);
        out[base + 2] = (float) (wp.z - entityPos.z);
    }

    private float[] computeHeadForward() {
        Optional<GeoBone> head = this.getGeoModel().getBone("Head");
        if (head.isEmpty()) return null;
        Vector3d h = head.get().getWorldPosition();

        Optional<GeoBone> jaw = this.getGeoModel().getBone("Lower Jaw");
        double dx, dy, dz;
        if (jaw.isPresent()) {
            Vector3d j = jaw.get().getWorldPosition();
            dx = j.x - h.x; dy = j.y - h.y; dz = j.z - h.z;
        } else {
            Matrix4f m = head.get().getWorldSpaceMatrix();
            Vector4f o = m.transform(new Vector4f(0f, 0f, 0f, 1f));
            Vector4f f = m.transform(new Vector4f(0f, 0f, -16f, 1f));
            dx = f.x() - o.x(); dy = f.y() - o.y(); dz = f.z() - o.z();
        }
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-6) return null;
        return new float[] { (float) (dx / len), (float) (dy / len), (float) (dz / len) };
    }

    @Override
    protected float getDeathMaxRotation(PurpleWormEntity animatable) {
        return 0f;
    }
}
