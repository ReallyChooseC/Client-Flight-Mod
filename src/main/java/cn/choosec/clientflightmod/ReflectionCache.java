package cn.choosec.clientflightmod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static cn.choosec.clientflightmod.ClientFlightMod.LOGGER;

@SuppressWarnings("FieldCanBeLocal")
public class ReflectionCache {
    private static Class<?> tweakerooConfigsClass;
    private static Class<?> tweakerooFeaturesClass;
    private static Class<?> cameraClass;

    private static Field tweakSprintField;
    private static Field tweakFlySpeedField;

    private static Method getBooleanValueMethod;
    private static Method getDoubleValueMethod;
    private static Method getActiveFlySpeedConfigMethod;
    private static Method getCameraMethod;

    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        try {
            tweakerooConfigsClass = Class.forName("fi.dy.masa.tweakeroo.config.Configs");
            tweakerooFeaturesClass = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
            cameraClass = Class.forName("fi.dy.masa.tweakeroo.util.CameraEntity");

            tweakSprintField = tweakerooFeaturesClass.getField("TWEAK_PERMANENT_SPRINT");
            tweakFlySpeedField = tweakerooFeaturesClass.getField("TWEAK_FLY_SPEED");

            getActiveFlySpeedConfigMethod = tweakerooConfigsClass.getMethod("getActiveFlySpeedConfig");
            getCameraMethod = cameraClass.getMethod("getCamera");

            Object featureToggleSample = tweakSprintField.get(null);
            getBooleanValueMethod = featureToggleSample.getClass().getMethod("getBooleanValue");

            Object configSample = getActiveFlySpeedConfigMethod.invoke(null);
            getDoubleValueMethod = configSample.getClass().getMethod("getDoubleValue");

            initialized = true;
            LOGGER.info("Successfully initialized Tweakeroo reflection cache");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Tweakeroo reflection cache", e);
        }
    }

    public static Field getTweakSprintField() { return tweakSprintField; }
    public static Field getTweakFlySpeedField() { return tweakFlySpeedField; }
    public static Method getGetBooleanValueMethod() { return getBooleanValueMethod; }
    public static Method getGetDoubleValueMethod() { return getDoubleValueMethod; }
    public static Method getGetActiveFlySpeedConfigMethod() { return getActiveFlySpeedConfigMethod; }
    public static Method getGetCameraMethod() { return getCameraMethod; }
    public static boolean isInitialized() { return initialized; }
}