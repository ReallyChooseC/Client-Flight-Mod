package com.clientflightmod.mixin;

import com.clientflightmod.ClientFlightMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(
        method = "isSpectator",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;isCreative()Z"
        )
    )
    private boolean redirectSpectatorCheck(PlayerEntity instance) {
        return ClientFlightMod.isFlightEnabled() || instance.isCreative();
    }
}