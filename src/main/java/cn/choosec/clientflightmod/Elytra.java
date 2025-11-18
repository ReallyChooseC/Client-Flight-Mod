package cn.choosec.clientflightmod;

import static cn.choosec.clientflightmod.ClientFlightMod.*;
import static cn.choosec.clientflightmod.Config.saveConfig;
import static cn.choosec.clientflightmod.Feedback.*;

import net.minecraft.network.chat.Component;

public class Elytra {
    public static boolean checkPermanentSprint() {
        if (!ReflectionCache.isInitialized()) return false;

        try {
            Object tweakSprint = ReflectionCache.getTweakSprintField().get(null);
            return (boolean) ReflectionCache.getGetBooleanValueMethod().invoke(tweakSprint);
        } catch (Exception e) {
            LOGGER.error("Failed to check permanent sprint status", e);
            return false;
        }
    }

    public static double calculateSpeed(boolean sprinting, boolean applySprint) {
        if (!ReflectionCache.isInitialized()) {
            double base = speed * SCALE_FACTOR;
            return applySprint && sprinting ? base * 2 : base;
        }

        try {
            Object tweakFlySpeed = ReflectionCache.getTweakFlySpeedField().get(null);
            boolean speedEnabled = (boolean) ReflectionCache.getGetBooleanValueMethod().invoke(tweakFlySpeed);

            double tweakValue = BASE_TWEAKEROO;
            if (speedEnabled) {
                Object speedConfig = ReflectionCache.getGetActiveFlySpeedConfigMethod().invoke(null);
                tweakValue = (double) ReflectionCache.getGetDoubleValueMethod().invoke(speedConfig);
            }

            double base = speed * (tweakValue / BASE_TWEAKEROO) * SCALE_FACTOR;
            return applySprint && sprinting ? base * 2 : base;
        } catch (Exception e) {
            LOGGER.error("Failed to calculate speed from Tweakeroo config", e);
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
