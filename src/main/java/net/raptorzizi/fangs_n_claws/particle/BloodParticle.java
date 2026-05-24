package net.raptorzizi.fangs_n_claws.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

public class BloodParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    public BloodParticle(ClientLevel level, double x, double y, double z,
                         SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.sprites = spriteSet;
        this.gravity  = 1.0F;
        this.lifetime = 5 + (int)(Math.random() * 10);
        this.scale(3f);
        this.setSpriteFromAge(spriteSet);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.onGround) {
            this.level.addParticle(ParticlesRegistry.BLOOD_GROUND.get(),
                    this.x, this.y, this.z, 0.0, 0.0, 0.0);
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
            return new BloodParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
