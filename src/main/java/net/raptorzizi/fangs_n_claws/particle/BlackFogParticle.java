package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlackFogParticle extends TextureSheetParticle {

    public BlackFogParticle(ClientLevel level, double x, double y, double z,
                             SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.hasPhysics = false;
        this.gravity    = 0.0f;
        this.lifetime   = 30 + level.random.nextInt(20); // 30-50 ticks
        this.quadSize   = 0.6f + level.random.nextFloat() * 0.8f;
        this.rCol = 0.04f;
        this.gCol = 0.04f;
        this.bCol = 0.04f;
        this.alpha = 0.75f;
        this.pickSprite(spriteSet);
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
        this.xd *= 0.94;
        this.yd *= 0.94;
        this.zd *= 0.94;
        this.move(this.xd, this.yd, this.zd);
        this.quadSize *= 1.025f;
        this.alpha = 0.75f * (1f - (float) this.age / this.lifetime);
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
            return new BlackFogParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
