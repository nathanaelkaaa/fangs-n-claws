package net.raptorzizi.fangs_n_claws.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.raptorzizi.fangs_n_claws.biome.FncBiomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesMixin {

    @Inject(method = "createLookup()Lnet/minecraft/core/HolderLookup$Provider;", at = @At("HEAD"))
    private static void fnc$enterVanillaLookup(CallbackInfoReturnable<HolderLookup.Provider> cir) {
        FncBiomes.setBuildingVanillaLookup(true);
    }

    @Inject(method = "createLookup()Lnet/minecraft/core/HolderLookup$Provider;", at = @At("RETURN"))
    private static void fnc$exitVanillaLookup(CallbackInfoReturnable<HolderLookup.Provider> cir) {
        FncBiomes.setBuildingVanillaLookup(false);
    }
}
