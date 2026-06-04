package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FireExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean mirrored;

    public FireExplosionParticle(ClientLevel level, double x, double y, double z,
                                  SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(this.random.nextFloat() * 3.0f + 2.5f);
        this.lifetime = 20 + (int) (this.random.nextFloat() * 20);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.0025F;
        this.mirrored = this.random.nextBoolean();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.alpha = Mth.clampedLerp(1, 0, (age - lifetime + 8) / 8f);
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            move(xd, yd, zd);
            this.xd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);
            this.yd += this.random.nextFloat() / 100.0F - this.gravity;
            this.zd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);
            this.setSpriteFromAge(this.sprites);
            this.scale(1.02f);
        }
    }

    @Override
    protected float getU0() {
        return mirrored ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return mirrored ? super.getU0() : super.getU1();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
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
            return new FireExplosionParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
