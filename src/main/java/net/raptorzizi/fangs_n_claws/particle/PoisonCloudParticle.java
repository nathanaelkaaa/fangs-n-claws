package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoisonCloudParticle extends TextureSheetParticle {

    private static final float[] START = { 0.4627f, 0.9451f, 0.3294f };
    private static final float[] END   = { 0.1373f, 0.8314f, 0.2824f };

    protected PoisonCloudParticle(ClientLevel level, double x, double y, double z,
                                  double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.scale(7.0F);
        this.setSize(0.25F, 0.25F);
        this.lifetime = this.random.nextInt(40) + 100;
        this.gravity = 3.0E-6F;
        this.xd = xd;
        this.yd = yd + this.random.nextFloat() / 500.0F;
        this.zd = zd;
        this.pickSprite(sprites);
        this.setAlpha(0.9F);
        applyGradient(0.0f);
    }

    private void applyGradient(float t) {
        this.setColor(
                Mth.lerp(t, START[0], END[0]),
                Mth.lerp(t, START[1], END[1]),
                Mth.lerp(t, START[2], END[2]));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && this.alpha > 0.0F) {
            this.xd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);

            float t = Mth.clamp((float) this.age / (float) this.lifetime, 0.0f, 1.0f);
            applyGradient(t);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }
        } else {
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float t = Mth.clamp(((float) this.age + scaleFactor) / (float) this.lifetime, 0.0f, 1.0f);
        return this.quadSize * (1.0f + t);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new PoisonCloudParticle(level, x, y, z, dx, dy, dz, this.sprites);
        }
    }
}
