package net.raptorzizi.fangs_n_claws.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AcidSplashParticle extends TextureSheetParticle {

    private static final float Y_OFFSET   = 0.05f;
    private static final int   FRAMES     = 12;
    private static final int   ANIM_TICKS = 60;
    private static final int   HOLD_TICKS = 40;

    private final float cosR;
    private final float sinR;

    protected AcidSplashParticle(ClientLevel level, double x, double y, double z, float scale, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.xd = this.yd = this.zd = 0.0;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.lifetime = ANIM_TICKS + HOLD_TICKS;
        this.quadSize = 2.0f * scale;
        this.rCol = this.gCol = this.bCol = 1.0f;
        this.alpha = 1.0f;
        float angle = (float) Math.toRadians(this.random.nextInt(4) * 90);
        this.cosR = Mth.cos(angle);
        this.sinR = Mth.sin(angle);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.age <= ANIM_TICKS) {
            this.alpha = 1.0f;
        } else {
            float held = (this.age - ANIM_TICKS) / (float) HOLD_TICKS;
            this.alpha = Mth.clamp(1.0f - held, 0.0f, 1.0f);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y()) + Y_OFFSET;
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z());
        float s = this.getQuadSize(partialTicks);
        float u0 = this.getU0(), u1 = this.getU1();
        int light = this.getLightColor(partialTicks);

        float progress = Mth.clamp((this.age + partialTicks) / (float) ANIM_TICKS, 0.0f, 0.99999f);
        int frame = (int) (progress * FRAMES);
        float vSpan = (this.getV1() - this.getV0()) / FRAMES;
        float v0 = this.getV0() + frame * vSpan;
        float v1 = v0 + vSpan;

        vertex(buffer, px, py, pz, -s, -s, u0, v1, light);
        vertex(buffer, px, py, pz, -s, +s, u0, v0, light);
        vertex(buffer, px, py, pz, +s, +s, u1, v0, light);
        vertex(buffer, px, py, pz, +s, -s, u1, v1, light);

        vertex(buffer, px, py, pz, +s, -s, u1, v1, light);
        vertex(buffer, px, py, pz, +s, +s, u1, v0, light);
        vertex(buffer, px, py, pz, -s, +s, u0, v0, light);
        vertex(buffer, px, py, pz, -s, -s, u0, v1, light);
    }

    private void vertex(VertexConsumer buffer, float px, float py, float pz,
                        float ox, float oz, float u, float v, int light) {
        float rx = ox * this.cosR - oz * this.sinR;
        float rz = ox * this.sinR + oz * this.cosR;
        buffer.addVertex(px + rx, py, pz + rz)
                .setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            float scale = dx > 0.0 ? (float) dx : 1.0f;
            return new AcidSplashParticle(level, x, y, z, scale, this.sprites);
        }
    }
}
