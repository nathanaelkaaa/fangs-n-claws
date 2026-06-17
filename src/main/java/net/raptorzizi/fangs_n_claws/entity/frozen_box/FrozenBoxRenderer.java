package net.raptorzizi.fangs_n_claws.entity.frozen_box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

import java.util.EnumMap;
import java.util.Map;

public class FrozenBoxRenderer extends GeoEntityRenderer<FrozenBoxEntity> {

    private final Map<Direction, float[][]> originalUVs = new EnumMap<>(Direction.class);

    public FrozenBoxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FrozenBoxModel());
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(FrozenBoxEntity entity) {
        return FangsClawsMod.id("textures/entity/frozen.png");
    }

    @Override
    public RenderType getRenderType(FrozenBoxEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public Color getRenderColor(FrozenBoxEntity animatable, float partialTick, int packedLight) {
        return Color.ofARGB(160, 255, 255, 255);
    }

    @Override
    public void preRender(PoseStack poseStack, FrozenBoxEntity entity, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        LivingEntity target = entity.getTarget();
        float w = target != null ? target.getBbWidth()  * 1.2f : 1f;
        float h = target != null ? target.getBbHeight() : 1f;

        poseStack.scale(w, h, w);
        applyAspectRatioUVs(model, w, h);

        super.preRender(poseStack, entity, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    private void applyAspectRatioUVs(BakedGeoModel model, float w, float h) {
        cacheOriginalUVsIfNeeded(model);
        float ratio = h / w;

        for (GeoBone bone : model.topLevelBones()) {
            for (GeoCube cube : bone.getCubes()) {
                for (GeoQuad quad : cube.quads()) {
                    Direction dir = quad.direction();
                    float[][] orig = originalUVs.get(dir);
                    if (orig == null) continue;

                    GeoVertex[] verts = quad.vertices();
                    boolean isSideFace = dir == Direction.NORTH || dir == Direction.SOUTH
                                     || dir == Direction.EAST  || dir == Direction.WEST;

                    for (int i = 0; i < verts.length; i++) {
                        float u = orig[i][0];
                        float v = orig[i][1];

                        if (isSideFace) {
                            float vMin = Float.MAX_VALUE, vMax = -Float.MAX_VALUE;
                            for (float[] o : orig) { vMin = Math.min(vMin, o[1]); vMax = Math.max(vMax, o[1]); }
                            float origRange = vMax - vMin;
                            float newRange  = origRange * ratio;
                            float t = origRange > 0 ? (v - vMin) / origRange : 0;
                            v = vMin + t * newRange;
                        }

                        verts[i] = verts[i].withUVs(u, v);
                    }
                }
            }
        }
    }

    private void cacheOriginalUVsIfNeeded(BakedGeoModel model) {
        if (!originalUVs.isEmpty()) return;

        for (GeoBone bone : model.topLevelBones()) {
            for (GeoCube cube : bone.getCubes()) {
                for (GeoQuad quad : cube.quads()) {
                    Direction dir = quad.direction();
                    if (originalUVs.containsKey(dir)) continue;

                    GeoVertex[] verts = quad.vertices();
                    float[][] uvs = new float[verts.length][2];
                    for (int i = 0; i < verts.length; i++) {
                        uvs[i][0] = verts[i].texU();
                        uvs[i][1] = verts[i].texV();
                    }
                    originalUVs.put(dir, uvs);
                }
            }
        }
    }
}
