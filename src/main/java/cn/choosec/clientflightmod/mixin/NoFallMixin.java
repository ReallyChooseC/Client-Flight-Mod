package cn.choosec.clientflightmod.mixin;

import cn.choosec.clientflightmod.Nofall;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.choosec.clientflightmod.ClientFlightMod.nofallToggle;

@Mixin(LocalPlayer.class)
public abstract class NoFallMixin {
    @Shadow
    protected abstract boolean isControlledCamera();

    @Unique
    private boolean clientFlightMod$initialized;

    @Unique
    private boolean clientFlightMod$wasEligibleAirborne;

    @Unique
    private int clientFlightMod$lastSendPositionTick;

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void clientFlightMod$prepareSafeLanding(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        boolean onGround = player.onGround();
        int currentTick = player.tickCount;
        boolean consecutiveTick = clientFlightMod$initialized
                && currentTick == clientFlightMod$lastSendPositionTick + 1;
        clientFlightMod$lastSendPositionTick = currentTick;
        boolean controlledCamera = isControlledCamera();

        if (!consecutiveTick || !controlledCamera) {
            clientFlightMod$initialized = true;
            clientFlightMod$wasEligibleAirborne = controlledCamera
                    && isEligibleAirborne(player, onGround);
            return;
        }

        if (onGround) {
            if (nofallToggle && clientFlightMod$wasEligibleAirborne && isEligibleLanding(player)) {
                Nofall.prepareSafeLanding(player);
            }
            clientFlightMod$wasEligibleAirborne = false;
        } else {
            clientFlightMod$wasEligibleAirborne = isEligibleAirborne(player, false);
        }
    }

    @Unique
    private static boolean isEligibleAirborne(LocalPlayer player, boolean onGround) {
        return !onGround
                && !player.isPassenger()
                && !player.isFallFlying()
                && !player.isInWater()
                && !player.isInLava()
                && !player.isSwimming()
                && !player.onClimbable();
    }

    @Unique
    private static boolean isEligibleLanding(LocalPlayer player) {
        return !player.isPassenger()
                && !player.isFallFlying()
                && !player.isInWater()
                && !player.isInLava()
                && !player.isSwimming()
                && !player.onClimbable();
    }
}
