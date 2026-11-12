package net.raptorzizi.fangs_n_claws.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;

public class AcidBlock extends LiquidBlock {

    // Particles
    private static final int SURFACE_CHANCE = 10;
    private static final int SUBMERGED_CHANCE = 40;
    private static final double SURFACE_OFFSET = 0.02;
    private static final double RISE_BASE = 0.015;
    private static final double RISE_SPREAD = 0.02;
    private static final double DRIFT = 0.04;

    // Ambient sound
    private static final int SOUND_CHANCE = 1000;
    private static final float VOLUME_BASE = 0.2F;
    private static final float VOLUME_SPREAD = 0.2F;
    private static final float PITCH_BASE = 0.9F;
    private static final float PITCH_SPREAD = 0.15F;

    public AcidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level,
                            @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean surface = level.getFluidState(pos.above()).isEmpty();

        if (surface && random.nextInt(SOUND_CHANCE) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    SoundsRegistry.PURPLE_WORM_ACID_PUDDLE.get(), SoundSource.BLOCKS,
                    VOLUME_BASE + random.nextFloat() * VOLUME_SPREAD,
                    PITCH_BASE + random.nextFloat() * PITCH_SPREAD, false);
        }

        if (random.nextInt(surface ? SURFACE_CHANCE : SUBMERGED_CHANCE) != 0) return;

        double height = surface
                ? state.getFluidState().getHeight(level, pos) + SURFACE_OFFSET
                : random.nextDouble();

        level.addParticle(ParticlesRegistry.ACID_BUBBLE.get(),
                pos.getX() + random.nextDouble(),
                pos.getY() + height,
                pos.getZ() + random.nextDouble(),
                (random.nextDouble() - 0.5) * DRIFT,
                RISE_BASE + random.nextDouble() * RISE_SPREAD,
                (random.nextDouble() - 0.5) * DRIFT);
    }
}
