package com.clientflightmod;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.Properties;

public class ClientFlightMod implements ClientModInitializer {
    private static KeyBinding flyKey;
    static final File CONFIG_FILE = new File("config/clientflight.cfg");
    static boolean elytraToggle = true;
    static boolean nofallToggle = true;
    static double speed = 1.0;
    static final double BASE_TWEAKEROO = 0.064;
    static final double SCALE_FACTOR = 0.703;
    static final double VERTICAL_RATIO = 0.689;
    static final String TWEAKEROO_CONFIGS = "fi.dy.masa.tweakeroo.config.Configs";
    static final String TWEAKEROO_FEATURES = "fi.dy.masa.tweakeroo.config.FeatureToggle";

    @Override
    public void onInitializeClient() {
        loadConfig();
        flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientflightmod.toggleflight",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.clientflightmod.main"));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("cfly")
                .then(ClientCommandManager.literal("toggle").executes(ctx -> { toggleFlight(); return 1; }))
                .then(ClientCommandManager.literal("elytratoggle").executes(ctx -> { toggleElytra(); return 1; }))
                .then(ClientCommandManager.literal("nofalltoggle").executes(ctx -> { toggleNofall(); return 1; }))
                .then(ClientCommandManager.literal("speed")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                .executes(ctx -> { setSpeed(DoubleArgumentType.getDouble(ctx, "value")); return 1; }))

                )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (flyKey.wasPressed()) toggleFlight();
            handleElytraMovement(client);
            NofallDamage(client);
        });
    }

    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) createDefaultConfig();
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                Properties props = new Properties();
                props.load(input);
                elytraToggle = Boolean.parseBoolean(props.getProperty("elytratoggle", "true"));
                speed = Math.max(0, Double.parseDouble(props.getProperty("speed", "1.0")));
            }
        } catch (Exception e) { System.err.println("Failed to load config"); }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createDefaultConfig() throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", "true");
            props.setProperty("speed", "1.0");
            props.store(output, null);
        }
    }

    private static synchronized void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", String.valueOf(elytraToggle));
            props.setProperty("speed", String.valueOf(speed));
            props.store(output, null);
        } catch (IOException e) { System.err.println("Failed to save config"); }
    }

    private void NofallDamage(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        if (player.getAbilities().allowFlying) {
            player.networkHandler
                    .sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }

    private static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().allowFlying || !player.isFallFlying()) return;

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

    private static boolean checkPermanentSprint() {
        try {
            Class<?> featuresClass = Class.forName(TWEAKEROO_FEATURES);
            Object tweakSprint = featuresClass.getField("TWEAK_PERMANENT_SPRINT").get(null);
            return (boolean) tweakSprint.getClass().getMethod("getBooleanValue").invoke(tweakSprint);
        } catch (Exception e) {
            return false;
        }
    }

    private static void toggleFlight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ClientPlayerEntity player = client.player;
        boolean state = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = state;
        if (!state) player.getAbilities().flying = false;
        String statusKey = "clientflightmod." + (state ? "enabled" : "disabled");
        Text message = Text.translatable("clientflightmod.fly")
                .append(Text.translatable(": "))
                .append(Text.translatable(statusKey));
        sendCustomFeedback(message);
    }

    private static void toggleElytra() {
        elytraToggle = !elytraToggle;
        saveConfig();
        Text message = Text.translatable("clientflightmod.elytra_toggle")
                .append(Text.translatable(": "))
                .append(Text.translatable("clientflightmod." + (elytraToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }

    private static void setSpeed(double value) {
        speed = Math.max(0, value);
        saveConfig();
        sendFeedback("clientflightmod.speed_set", speed);
    }

    private static void sendFeedback(String key, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(key, args), true);
        }
    }

    private static void sendCustomFeedback(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(message, true);
        }
    }
}