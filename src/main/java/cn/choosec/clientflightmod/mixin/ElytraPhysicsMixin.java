package cn.choosec.clientflightmod.mixin;

import cn.choosec.clientflightmod.ReflectionCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity instanceof ClientPlayerEntity player) {
            if (player.isGliding() && elytraToggle && player.getAbilities().allowFlying) {
                ci.cancel();
                handleElytraMovement(MinecraftClient.getInstance());
            }
        }
    }

    @Unique
    private static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().allowFlying || !player.isGliding()) return;

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
        boolean sprinting = checkPermanentSprint() || player.input.playerInput.sprint();
        float forward = 0.0f;
        if (player.input.playerInput.forward() != player.input.playerInput.backward()) {
            forward = player.input.playerInput.forward() ? 1.0f : -1.0f;
        }
        float sideways = 0.0f;
        if (player.input.playerInput.left() != player.input.playerInput.right()) {
            sideways = player.input.playerInput.left() ? 1.0f : -1.0f;
        }
        //#else
        //$$ GameOptions options = client.options;
        //$$ boolean sprinting = checkPermanentSprint() || options.sprintKey.isPressed();
        //$$ float forward = player.input.movementForward;
        //$$ float sideways = player.input.movementSideways;
        //#endif

        Vec3d horizontal = Vec3d.ZERO;
        if (forward != 0 || sideways != 0) {
            float yaw = (float) Math.toRadians(player.getYaw());
            Vec3d dir = new Vec3d(
                    -MathHelper.sin(yaw) * forward + MathHelper.cos(yaw) * sideways,
                    0,
                    MathHelper.cos(yaw) * forward + MathHelper.sin(yaw) * sideways
            ).normalize();
            horizontal = dir.multiply(calculateSpeed(sprinting, true));
        }

        double vertical = 0;
        if (!freeCameraActive) {
            float verticalways = 0.0f;
            //#if MC>12101
            if (player.input.playerInput.jump() != player.input.playerInput.sneak()) {
                verticalways = player.input.playerInput.jump() ? 1.0f : -1.0f;
            //#else
            //$$ if (player.input.jumping != player.input.sneaking) {
            //$$     verticalways = player.input.jumping ? 1.0f : -1.0f;
            //#endif
            }
            vertical = calculateSpeed(false, false) * VERTICAL_RATIO * verticalways;
        }

        player.setVelocity(horizontal.add(0, vertical, 0));
        player.velocityModified = true;

        player.move(MovementType.SELF, player.getVelocity());
    }
}