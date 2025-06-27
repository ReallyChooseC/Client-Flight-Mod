package cn.choosec.clientflightmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static cn.choosec.clientflightmod.ClientFlightMod.*;
import static cn.choosec.clientflightmod.Config.saveConfig;
import static cn.choosec.clientflightmod.Feedback.*;

public class Elytra {
    static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().allowFlying || !player.isFallFlying()) return;

        boolean freeCameraActive = false;
        try {
            Class<?> cameraClass = Class.forName("fi.dy.masa.tweakeroo.util.CameraEntity");
            Object cameraInstance = cameraClass.getMethod("getCamera").invoke(null);
            freeCameraActive = cameraInstance != null;
        } catch (Exception ignored) {}

        boolean sprinting = checkPermanentSprint() || player.input.playerInput.sprint();
        float forward = 0.0f;
        if (player.input.playerInput.forward() != player.input.playerInput.backward()) {
            forward = player.input.playerInput.forward() ? 1.0f : -1.0f;
        }
        float sideways = 0.0f;
        if (player.input.playerInput.left() != player.input.playerInput.right()) {
            sideways = player.input.playerInput.left() ? 1.0f : -1.0f;
        }

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
            if (player.input.playerInput.jump() != player.input.playerInput.sneak()) {
                verticalways = player.input.playerInput.jump() ? 1.0f : -1.0f;
            }
            vertical = calculateSpeed(false, false) * VERTICAL_RATIO * verticalways;
        }

        player.setVelocity(horizontal.add(0, vertical, 0));
        player.velocityModified = true;
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
        Text message = Text.translatable("clientflightmod.elytra_toggle")
                .append(Text.literal(": "))
                .append(Text.translatable("clientflightmod." + (elytraToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }

    static void setSpeed(double value) {
        speed = Math.max(0, value);
        saveConfig();
        sendFeedback("clientflightmod.speed_set", speed);
    }
}
