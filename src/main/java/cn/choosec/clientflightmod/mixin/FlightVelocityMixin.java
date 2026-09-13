package cn.choosec.clientflightmod.mixin;

import cn.choosec.clientflightmod.Flight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
//#if MC>=12109
//$$ import net.minecraft.world.phys.Vec3;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPacketListener.class)
public class FlightVelocityMixin {
    //#if MC>=12109
    //$$ @ModifyArgs(method = "handleSetEntityMotion", at = @At(value = "INVOKE",
    //$$         target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V"))
    //#else
    @ModifyArgs(method = "handleSetEntityMotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;lerpMotion(DDD)V"))
    //#endif
    private void clientFlightMod$keepFlight(Args args, ClientboundSetEntityMotionPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        //#if MC>=260000
        //$$ int entityId = packet.id();
        //#else
        int entityId = packet.getId();
        //#endif
        if (player == null || player.getId() != entityId || player.hurtTime <= 0 || !Flight.isClientFlying(player)) return;

        // Damage can resend gravity accumulated by a server that has not granted flight.
        //#if MC>=12109
        //$$ Vec3 velocity = args.get(0);
        //$$ if (velocity.y < 0) args.set(0, new Vec3(velocity.x, player.getDeltaMovement().y, velocity.z));
        //#else
        if ((double) args.get(1) < 0) args.set(1, player.getDeltaMovement().y);
        //#endif
    }
}
