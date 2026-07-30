package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AcidBubbleParticle extends TextureSheetParticle {

    private static final int POP_FRAMES = 5;

    private final SpriteSet spriteSet;

    protected AcidBubbleParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd, float sizeMul, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.spriteSet = sprites;
        this.quadSize *= (0.9F + this.random.nextFloat() * 0.5F) * sizeMul * 2.0F;
        this.hasPhysics = true;
        this.friction = 0.95F;
        this.lifetime = 10 + this.random.nextInt(10);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.setSprite(sprites.get(0, POP_FRAMES));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        int popStart = this.lifetime - POP_FRAMES;
        int frame = this.age >= popStart ? Math.min(this.age - popStart + 1, POP_FRAMES) : 0;
        this.setSprite(this.spriteSet.get(frame, POP_FRAMES));
        if (frame > 0) {
            this.xd = this.yd = this.zd = 0.0;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            return new AcidBubbleParticle(level, x, y, z, dx, dy, dz, 1.0F, this.sprites);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public TrailProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            return new AcidBubbleParticle(level, x, y, z, dx, dy, dz, 0.6F, this.sprites);
        }
    }
}
