package cn.choosec.clientflightmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static cn.choosec.clientflightmod.ClientFlightMod.*;
import static cn.choosec.clientflightmod.Config.saveConfig;
import static cn.choosec.clientflightmod.Feedback.*;

public class Elytra {
    static void handleElytraMovement(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().mayfly || !player.isFallFlying()) return;

        boolean freeCameraActive = false;
        try {
            Class<?> cameraClass = Class.forName("fi.dy.masa.tweakeroo.util.CameraEntity");
            Object cameraInstance = cameraClass.getMethod("getCamera").invoke(null);
            freeCameraActive = cameraInstance != null;
        } catch (Exception ignored) {}

        KeyMapping[] keyMappings = client.options.keyMappings;
        boolean sprinting = checkPermanentSprint() || client.options.keySprint.isDown();
        float forward = player.input.forwardImpulse;
        float sideways = player.input.leftImpulse;

        Vec3 horizontal = Vec3.ZERO;
        if (forward != 0 || sideways != 0) {
            float yaw = (float) Math.toRadians(player.getYRot());
            Vec3 dir = new Vec3(
                    -Mth.sin(yaw) * forward + Mth.cos(yaw) * sideways,
                    0,
                    Mth.cos(yaw) * forward + Mth.sin(yaw) * sideways
            ).normalize();
            horizontal = dir.multiply(calculateSpeed(sprinting, true), 0, calculateSpeed(sprinting, true));
        }

        double vertical = 0;
        if (!freeCameraActive) {
            float verticalways = 0.0f;
            if (client.options.keyJump.isDown() != client.options.keyShift.isDown()) {
                verticalways = client.options.keyJump.isDown() ? 1.0f : -1.0f;
            }
            vertical = calculateSpeed(false, false) * VERTICAL_RATIO * verticalways;
        }

        player.setDeltaMovement(horizontal.x, vertical, horizontal.z);
        player.hurtMarked = true;
    }

    private static boolean checkPermanentSprint() {
        try {
            Class<?> featuresClass = Class.forName(TWEAKEROO_FEATURES);
            Object tweakSprint = featuresClass.getField("TWEAK_PERMANENT_SPRINT").get(null);
            return (boolean) tweakSprint.getClass().getMethod("getBooleanValue").invoke(tweakSprint);
        } catch (Exception e) {
            return false;
        }
    }

    private static double calculateSpeed(boolean sprinting, boolean applySprint) {
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
        Component message = Component.translatable("clientflightmod.elytra_toggle")
                .append(Component.literal(": "))
                .append(Component.translatable("clientflightmod." + (elytraToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }

    static void setSpeed(double value) {
        speed = Math.max(0, value);
        saveConfig();
        sendFeedback("clientflightmod.speed_set", speed);
    }
}