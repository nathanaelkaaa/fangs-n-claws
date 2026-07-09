package net.raptorzizi.fangs_n_claws.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.raptorzizi.fangs_n_claws.client.OwlFlightState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void fnc$owlArmPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player) {
            OwlFlightState.applyArmPose((PlayerModel<?>) (Object) this, player, ageInTicks);
        }
    }
}
