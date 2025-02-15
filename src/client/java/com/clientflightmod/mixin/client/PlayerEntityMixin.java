package com.clientflightmod;

import com.clientflightmod.ClientFlightModClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    private void forceSpectatorAbilities(CallbackInfoReturnable<Boolean> cir) {
        if (ClientFlightModClient.isFlightEnabled()) {
            cir.setReturnValue(true);
        }
    }
}