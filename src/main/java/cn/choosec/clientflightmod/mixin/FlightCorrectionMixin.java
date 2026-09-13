package cn.choosec.clientflightmod.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

//#if MC>=12104
import cn.choosec.clientflightmod.CollisionProbe;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

@Mixin(ClientPacketListener.class)
public class FlightCorrectionMixin {
    //#if MC>=12104
    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;change()Lnet/minecraft/world/entity/PositionMoveRotation;"),
            cancellable = true)
    private void clientFlightMod$keepFlightCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (CollisionProbe.receive(packet)) ci.cancel();
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void clientFlightMod$finishCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        CollisionProbe.finish();
    }
    //#endif
}
