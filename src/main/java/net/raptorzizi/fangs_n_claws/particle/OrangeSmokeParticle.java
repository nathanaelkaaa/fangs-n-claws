package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OrangeSmokeParticle extends BaseAshSmokeParticle {

    protected OrangeSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double xd, double yd, double zd,
                                   float quadSizeMultiplier, SpriteSet sprites) {
        super(level, x, y, z, 0.1F, 0.1F, 0.1F, xd, yd, zd, quadSizeMultiplier, sprites, 0.3F, 8, -0.1F, true);
        float r = 0.7f + this.random.nextFloat() * 0.3f;
        float g = 0.2f + this.random.nextFloat() * 0.3f;
        float b = this.random.nextFloat() * 0.08f;
        this.setColor(r, g, b);
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
            return new OrangeSmokeParticle(level, x, y, z, dx, dy, dz, 1.0F, this.sprites);
        }
    }
}
