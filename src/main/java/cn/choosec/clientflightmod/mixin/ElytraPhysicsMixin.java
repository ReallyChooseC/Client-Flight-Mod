package cn.choosec.clientflightmod.mixin;

import cn.choosec.clientflightmod.ReflectionCache;
import net.minecraft.client.Minecraft;
//#if MC<=12101
//$$ import net.minecraft.client.Options;
//#endif
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.choosec.clientflightmod.ClientFlightMod.*;
import static cn.choosec.clientflightmod.Elytra.*;

@Mixin(LivingEntity.class)
public class ElytraPhysicsMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3 movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity instanceof LocalPlayer player) {
            if (player.isFallFlying() && elytraToggle && player.getAbilities().mayfly) {
                ci.cancel();
                handleElytraMovement(Minecraft.getInstance());
            }
        }
    }

    @Unique
    private static void handleElytraMovement(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().mayfly || !player.isFallFlying()) return;

        boolean freeCameraActive = false;
        if (ReflectionCache.isInitialized()) {
            try {
                Object cameraInstance = ReflectionCache.getGetCameraMethod().invoke(null);
                freeCameraActive = cameraInstance != null;
            } catch (Exception e) {
                LOGGER.debug("Failed to check camera status", e);
            }
        }

        //#if MC>12101
        boolean sprinting = checkPermanentSprint() || player.input.keyPresses.sprint();
        float forward = 0.0f;
        if (player.input.keyPresses.forward() != player.input.keyPresses.backward()) {
            forward = player.input.keyPresses.forward() ? 1.0f : -1.0f;
        }
        float sideways = 0.0f;
        if (player.input.keyPresses.left() != player.input.keyPresses.right()) {
            sideways = player.input.keyPresses.left() ? 1.0f : -1.0f;
        }
        //#else
        //$$ Options options = client.options;
        //$$ boolean sprinting = checkPermanentSprint() || options.keySprint.isDown();
        //$$ float forward = player.input.forwardImpulse;
        //$$ float sideways = player.input.leftImpulse;
        //#endif

        Vec3 horizontal = Vec3.ZERO;
        if (forward != 0 || sideways != 0) {
            float yaw = (float) Math.toRadians(player.getYRot());
            Vec3 dir = new Vec3(
                    -Mth.sin(yaw) * forward + Mth.cos(yaw) * sideways,
                    0,
                    Mth.cos(yaw) * forward + Mth.sin(yaw) * sideways
            ).normalize();
            horizontal = dir.scale(calculateSpeed(sprinting, true));
        }

        double vertical = 0;
        if (!freeCameraActive) {
            float verticalways = 0.0f;
            //#if MC>12101
            if (player.input.keyPresses.jump() != player.input.keyPresses.shift()) {
                verticalways = player.input.keyPresses.jump() ? 1.0f : -1.0f;
            //#else
            //$$ if (player.input.jumping != player.input.shiftKeyDown) {
            //$$     verticalways = player.input.jumping ? 1.0f : -1.0f;
            //#endif
            }
            vertical = calculateSpeed(false, false) * VERTICAL_RATIO * verticalways;
        }

        player.setDeltaMovement(horizontal.add(0, vertical, 0));
        player.hurtMarked = true;

        player.move(MoverType.SELF, player.getDeltaMovement());
    }
}