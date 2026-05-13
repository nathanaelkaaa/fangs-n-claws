package net.raptorzizi.fangs_n_claws.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class StunStarParticle extends Particle {

    private static final ResourceLocation CENTER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/particle/stun_star.png");
    private static final ResourceLocation TRAIL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "textures/particle/teletor_trail.png");

    private static final int   TRAIL_LENGTH  = 64;
    private static final int   SAMPLE_COUNT  = 10;
    private static final float TRAIL_HEIGHT  = 0.4f;
    private static final float STAR_SIZE     = 0.225f;

    float trailR = 1f, trailG = 1f, trailB = 1f, trailA = 0f;

    protected Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    private int trailPointer = -1;

    private final int     entityId;
    private final float   offset;
    private final boolean reverseOrbit;

    protected StunStarParticle(ClientLevel level, double x, double y, double z,
                                int entityId, float offset) {
        super(level, x, y, z);
        this.xd           = 0;
        this.yd           = 0;
        this.zd           = 0;
        this.entityId     = entityId;
        this.offset       = offset;
        this.hasPhysics   = false;
        this.gravity      = 0f;
        this.reverseOrbit = random.nextBoolean();
        this.lifetime     = 30 + random.nextInt(5);
        this.alpha        = 1f;
        this.setColor(1.35f, 1.35f, 1.35f);

        Vec3 start = getOrbitPos();
        this.setPos(start.x, start.y, start.z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    private void tickTrail() {
        Vec3 current = new Vec3(this.x, this.y, this.z);
        if (trailPointer == -1) {
            for (int i = 0; i < trailPositions.length; i++) {
                trailPositions[i] = current;
            }
        }
        if (++trailPointer == trailPositions.length) {
            trailPointer = 0;
        }
        trailPositions[trailPointer] = current;
    }

    private Vec3 getTrailPosition(int pointer, float partialTick) {
        if (this.removed) partialTick = 1f;
        int i = this.trailPointer - pointer & 63;
        int j = this.trailPointer - pointer - 1 & 63;
        Vec3 d0 = trailPositions[j];
        Vec3 d1 = trailPositions[i].subtract(d0);
        return d0.add(d1.scale(partialTick));
    }


    @Override
    public void tick() {
        this.xd *= 0.9;
        this.yd *= 0.9;
        this.zd *= 0.9;
        this.oRoll = this.roll;
        this.roll  = (float)(Math.PI * Math.sin(this.age * 0.6F) * 0.3F);

        tickTrail();
        super.tick();

        this.trailA = 0.2F * Mth.clamp(this.age / (float) this.lifetime * 32f, 0f, 1f);

        if (entityId != -1) {
            Vec3 orbit = getOrbitPos();
            this.setPos(orbit.x, orbit.y, orbit.z);

            Entity entity = this.level.getEntity(entityId);
            if (entity instanceof LivingEntity living && !living.hasEffect(MobEffectsRegistry.STUNNED)) {
                this.remove();
            }
        }
    }

    @Override
    public void render(VertexConsumer ignored, Camera camera, float partialTick) {
        if (this.age < 2) return;

        Vec3 cameraPos = camera.getPosition();
        float cx = (float)(Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float cy = (float)(Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float cz = (float)(Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        Quaternionf quaternion;
        if (this.roll == 0f) {
            quaternion = camera.rotation();
        } else {
            quaternion = new Quaternionf(camera.rotation());
            float roll = Mth.lerp(partialTick, this.oRoll, this.roll);
            quaternion.mul(Axis.ZP.rotation(roll));
        }

        Vector3f[] corners = {
            new Vector3f(-1f, -1f, 0f),
            new Vector3f(-1f,  1f, 0f),
            new Vector3f( 1f,  1f, 0f),
            new Vector3f( 1f, -1f, 0f)
        };
        for (Vector3f corner : corners) {
            corner.rotate(quaternion);
            corner.mul(STAR_SIZE);
            corner.add(cx, cy, cz);
        }

        int red   = Math.min(255, (int)(this.rCol  * 255f));
        int green = Math.min(255, (int)(this.gCol  * 255f));
        int blue  = Math.min(255, (int)(this.bCol  * 255f));
        int alpha = Math.min(255, (int)(this.alpha * 255f));
        int lm    = getLightColor(partialTick);

        PoseStack ps = new PoseStack();
        PoseStack.Pose pose = ps.last();

        VertexConsumer starVc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(CENTER_TEXTURE));
        starVc.addVertex(corners[0].x(), corners[0].y(), corners[0].z()).setColor(red, green, blue, alpha).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lm & 0xffff, lm >> 16).setNormal(pose, -1f, 0f, 0f);
        starVc.addVertex(corners[1].x(), corners[1].y(), corners[1].z()).setColor(red, green, blue, alpha).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lm & 0xffff, lm >> 16).setNormal(pose, -1f, 0f, 0f);
        starVc.addVertex(corners[2].x(), corners[2].y(), corners[2].z()).setColor(red, green, blue, alpha).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lm & 0xffff, lm >> 16).setNormal(pose, -1f, 0f, 0f);
        starVc.addVertex(corners[3].x(), corners[3].y(), corners[3].z()).setColor(red, green, blue, alpha).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lm & 0xffff, lm >> 16).setNormal(pose, -1f, 0f, 0f);

        bufferSource.endBatch();

        if (trailPointer > -1) {
            VertexConsumer trailVc = bufferSource.getBuffer(RenderType.entityTranslucentCull(TRAIL_TEXTURE));

            PoseStack trailPs = new PoseStack();
            trailPs.pushPose();
            trailPs.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            double wx = Mth.lerp(partialTick, this.xo, this.x);
            double wy = Mth.lerp(partialTick, this.yo, this.y);
            double wz = Mth.lerp(partialTick, this.zo, this.z);

            Vec3 drawFrom = new Vec3(wx, wy, wz);
            float zRot        = -0.017453292F * camera.getXRot();
            Vec3 topAngleVec  = new Vec3(0, TRAIL_HEIGHT / 2f,  0).zRot(zRot);
            Vec3 botAngleVec  = new Vec3(0, TRAIL_HEIGHT / -2f, 0).zRot(zRot);

            int sampleMax = Math.min(SAMPLE_COUNT, this.lifetime - this.age);
            for (int s = 0; s < sampleMax; s++) {
                Vec3 sample = getTrailPosition(s, partialTick);
                float u1 = s / (float) sampleMax;
                float u2 = u1 + 1f / sampleMax;

                PoseStack.Pose trailPose = trailPs.last();
                Matrix4f mat4 = trailPose.pose();

                trailVc.addVertex(mat4, (float)drawFrom.x + (float)botAngleVec.x, (float)drawFrom.y + (float)botAngleVec.y, (float)drawFrom.z + (float)botAngleVec.z).setColor(trailR, trailG, trailB, trailA).setUv(u1, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lm).setNormal(trailPose, 0f, 1f, 0f);
                trailVc.addVertex(mat4, (float)sample.x  + (float)botAngleVec.x, (float)sample.y  + (float)botAngleVec.y, (float)sample.z  + (float)botAngleVec.z).setColor(trailR, trailG, trailB, trailA).setUv(u2, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lm).setNormal(trailPose, 0f, 1f, 0f);
                trailVc.addVertex(mat4, (float)sample.x  + (float)topAngleVec.x, (float)sample.y  + (float)topAngleVec.y, (float)sample.z  + (float)topAngleVec.z).setColor(trailR, trailG, trailB, trailA).setUv(u2, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lm).setNormal(trailPose, 0f, 1f, 0f);
                trailVc.addVertex(mat4, (float)drawFrom.x + (float)topAngleVec.x, (float)drawFrom.y + (float)topAngleVec.y, (float)drawFrom.z + (float)topAngleVec.z).setColor(trailR, trailG, trailB, trailA).setUv(u1, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lm).setNormal(trailPose, 0f, 1f, 0f);

                drawFrom = sample;
            }

            bufferSource.endBatch();
            trailPs.popPose();
        }
    }

    private Vec3 getOrbitPos() {
        Entity entity = this.level.getEntity(entityId);
        if (entity != null) {
            float angle = this.age * 10 + offset;
            Vec3 eyes = entity.getEyePosition()
                    .add(entity.getViewVector(0f).scale(entity.getBbWidth() * 0.85F))
                    .add(0, 0.5 + 0.12 * entity.getBbHeight(), 0);
            Vec3 orbitOffset = new Vec3(0, 0, entity.getBbWidth() * 0.5F + 0.2F)
                    .yRot(angle * (reverseOrbit ? -1 : 1) * (float)(Math.PI / 180F));
            return eyes.add(orbitOffset);
        }
        return Vec3.ZERO;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        public Provider(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            StunStarParticle p = new StunStarParticle(level, x, y, z, (int) dx, (float) dy);
            p.trailR = 1f;
            p.trailG = 1f;
            p.trailB = 1f;
            return p;
        }
    }
}
