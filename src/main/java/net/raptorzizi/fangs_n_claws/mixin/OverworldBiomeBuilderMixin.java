package net.raptorzizi.fangs_n_claws.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.raptorzizi.fangs_n_claws.biome.FncBiomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    // Mangrove swamp climate window
    private static final Climate.Parameter TEMPERATURE = Climate.Parameter.span(0.2F, 1.0F);
    private static final Climate.Parameter HUMIDITY = Climate.Parameter.span(-1.0F, 1.0F);
    private static final Climate.Parameter CONTINENTALNESS = Climate.Parameter.span(-0.11F, 1.0F);
    private static final Climate.Parameter EROSION = Climate.Parameter.span(0.55F, 1.0F);

    private static final float OFFSET = 3.0F;

    private static final Climate.Parameter DEPTH = Climate.Parameter.span(0.8F, 0.9F);

    @Inject(method = "addMidSlice(Ljava/util/function/Consumer;Lnet/minecraft/world/level/biome/Climate$Parameter;)V",
            at = @At("TAIL"))
    private void fnc$vermiciteCaveMidSlice(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
            Climate.Parameter weirdness, CallbackInfo ci) {
        fnc$addVermiciteCave(consumer, weirdness);
    }

    @Inject(method = "addLowSlice(Ljava/util/function/Consumer;Lnet/minecraft/world/level/biome/Climate$Parameter;)V",
            at = @At("TAIL"))
    private void fnc$vermiciteCaveLowSlice(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
            Climate.Parameter weirdness, CallbackInfo ci) {
        fnc$addVermiciteCave(consumer, weirdness);
    }

    @Inject(method = "addValleys(Ljava/util/function/Consumer;Lnet/minecraft/world/level/biome/Climate$Parameter;)V",
            at = @At("TAIL"))
    private void fnc$vermiciteCaveValleys(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
            Climate.Parameter weirdness, CallbackInfo ci) {
        fnc$addVermiciteCave(consumer, weirdness);
    }

    private void fnc$addVermiciteCave(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
            Climate.Parameter weirdness) {
        if (FncBiomes.isBuildingVanillaLookup()) return;

        consumer.accept(Pair.of(
                Climate.parameters(TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, weirdness, OFFSET),
                FncBiomes.VERMICITE_CAVE));
    }
}
