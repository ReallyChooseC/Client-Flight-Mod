package com.clientflightmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static com.clientflightmod.ClientFlightMod.*;
import static com.clientflightmod.Config.saveConfig;
import static com.clientflightmod.Feedback.*;

public class Elytra {
    static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().allowFlying || !player.isGliding()) return;

        boolean freeCameraActive = false;
        try {
            Class<?> cameraClass = Class.forName("fi.dy.masa.tweakeroo.util.CameraEntity");
            Object cameraInstance = cameraClass.getMethod("getCamera").invoke(null);
            freeCameraActive = cameraInstance != null;
        } catch (Exception ignored) {}

        GameOptions options = client.options;
        boolean sprinting = checkPermanentSprint() || options.sprintKey.isPressed();
        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;

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
            double verticalSpeed = calculateSpeed(false, false) * VERTICAL_RATIO;
            if (options.jumpKey.isPressed()) {
                vertical = verticalSpeed;
            } else if (options.sneakKey.isPressed()) {
                vertical = -verticalSpeed;
            }
        }

        player.setVelocity(horizontal.add(0, vertical, 0));
        player.velocityModified = true;
    }

    static boolean checkPermanentSprint() {
        try {
            Class<?> featuresClass = Class.forName(TWEAKEROO_FEATURES);
            Object tweakSprint = featuresClass.getField("TWEAK_PERMANENT_SPRINT").get(null);
            return (boolean) tweakSprint.getClass().getMethod("getBooleanValue").invoke(tweakSprint);
        } catch (Exception e) {
            return false;
        }
    }

    static double calculateSpeed(boolean sprinting, boolean applySprint) {
        try {
            Class<?> configsClass = Class.forName(TWEAKEROO_CONFIGS);
            Class<?> featuresClass = Class.forName(TWEAKEROO_FEATURES);

            Object tweakFlySpeed = featuresClass.getField("TWEAK_FLY_SPEED").get(null);
            boolean speedEnabled = (boolean) tweakFlySpeed.getClass().getMethod("getBooleanValue").invoke(tweakFlySpeed);

            double tweakValue = BASE_TWEAKEROO;
            if (speedEnabled) {
                Object speedConfig = configsClass.getMethod("getActiveFlySpeedConfig").invoke(null);
                tweakValue = (double) speedConfig.getClass().getMethod("getDoubleValue").invoke(speedConfig);
            }

            double base = speed * (tweakValue / BASE_TWEAKEROO) * SCALE_FACTOR;
            return applySprint && sprinting ? base * 2 : base;
        } catch (Exception e) {
            double base = speed * SCALE_FACTOR;
            return applySprint && sprinting ? base * 2 : base;
        }
    }

    static void toggleElytra() {
        elytraToggle = !elytraToggle;
        saveConfig();
        Text message = Text.translatable("clientflightmod.elytra_toggle")
                .append(Text.translatable(": "))
                .append(Text.translatable("clientflightmod." + (elytraToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }

    static void setSpeed(double value) {
        speed = Math.max(0, value);
        saveConfig();
        sendFeedback("clientflightmod.speed_set", speed);
    }
}
