package net.raptorzizi.fangs_n_claws.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.raptorzizi.fangs_n_claws.biome.FncBiomes;
import net.raptorzizi.fangs_n_claws.biome.VermiciteCavePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {

    @Unique
    private Holder<Biome> fnc$vermiciteCave;

    @Unique
    private boolean fnc$vermiciteCaveSearched;

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)"
            + "Lnet/minecraft/core/Holder;",
            at = @At("HEAD"), cancellable = true)
    private void fnc$vermiciteCave(int x, int y, int z, Climate.Sampler sampler,
                                   CallbackInfoReturnable<Holder<Biome>> callback) {
        if (QuartPos.toBlock(y) > VermiciteCavePlacement.MAX_BLOCK_Y) return;

        Holder<Biome> cave = this.fnc$vermiciteCave();
        if (cave == null) return;

        if (!VermiciteCavePlacement.matches(sampler.sample(x, y, z))) return;

        callback.setReturnValue(cave);
    }

    @Unique
    private Holder<Biome> fnc$vermiciteCave() {
        if (!this.fnc$vermiciteCaveSearched) {
            this.fnc$vermiciteCaveSearched = true;

            for (Holder<Biome> biome : ((BiomeSource) (Object) this).possibleBiomes()) {
                if (biome.is(FncBiomes.VERMICITE_CAVE)) {
                    this.fnc$vermiciteCave = biome;
                    break;
                }
            }
        }

        return this.fnc$vermiciteCave;
    }
}
