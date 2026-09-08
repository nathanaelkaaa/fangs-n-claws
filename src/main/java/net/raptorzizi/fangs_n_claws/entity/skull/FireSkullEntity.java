package net.raptorzizi.fangs_n_claws.entity.skull;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

public class FireSkullEntity extends SkullEntity {

    private static final int IGNITE_SECONDS = 5;

    public FireSkullEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected SoundEvent breathSound() { return SoundsRegistry.HELL_OGRE_FIRE_BREATH.get(); }

    @Override
    public String textureBaseName() { return "fire_skull"; }

    @Override
    protected void applyHitEffect(LivingEntity target) {
        target.igniteForSeconds(IGNITE_SECONDS);
    }

    @Override
    protected void spawnAmbientParticles() {
        emitAround(ParticlesRegistry.FIRE.get(), 0.02);
        if (this.random.nextInt(3) == 0) {
            emitAround(ParticlesRegistry.ORANGE_SMOKE.get(), 0.03);
        }
    }
}
