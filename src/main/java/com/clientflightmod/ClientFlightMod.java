package com.clientflightmod;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.Properties;

public class ClientFlightMod implements ClientModInitializer {
    private static KeyBinding flyKey;
    private static final File CONFIG_FILE = new File("config/clientflight.cfg");

    public static boolean elytraToggle = true;
    public static double speed = 0.2;
    public static double maxSpeed = 1.0;

    @Override
    public void onInitializeClient() {
        loadConfig();

        flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientflightmod.toggleflight",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.clientflightmod.main"
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("cfly")
                .then(ClientCommandManager.literal("toggle").executes(ctx -> { toggleFlight(); return 1; }))
                .then(ClientCommandManager.literal("elytratoggle").executes(ctx -> { toggleElytra(); return 1; }))
                .then(ClientCommandManager.literal("speed")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.01, 5.0))
                                .executes(ctx -> { setSpeed(DoubleArgumentType.getDouble(ctx, "value")); return 1; })))
                .then(ClientCommandManager.literal("maxspeed")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.1, 5.0))
                                .executes(ctx -> { setMaxSpeed(DoubleArgumentType.getDouble(ctx, "value")); return 1; })))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (flyKey.wasPressed()) toggleFlight();
            handleElytraMovement(client);
        });
    }

    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) createDefaultConfig();
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                Properties props = new Properties();
                props.load(input);
                elytraToggle = Boolean.parseBoolean(props.getProperty("elytratoggle", "true"));
                speed = clamp(Double.parseDouble(props.getProperty("speed", "0.2")), 0.01, 5.0);
                maxSpeed = clamp(Double.parseDouble(props.getProperty("maxspeed", "1.0")), 0.1, 5.0);
            }
        } catch (Exception e) { System.err.println("Failed to load config"); }
    }

    private static void createDefaultConfig() throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        Properties props = new Properties();
        props.setProperty("elytratoggle", "true");
        props.setProperty("speed", "0.2");
        props.setProperty("maxspeed", "1.0");
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, null);
        }
    }

    private static synchronized void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("elytratoggle", String.valueOf(elytraToggle));
            props.setProperty("speed", String.valueOf(speed));
            props.setProperty("maxspeed", String.valueOf(maxSpeed));
            props.store(output, null);
        } catch (IOException e) { System.err.println("Failed to save config"); }
    }

    private static void toggleFlight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ClientPlayerEntity player = client.player;
        boolean state = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = state;

        if (!state) player.getAbilities().flying = false;
        sendFeedback("clientflightmod.fly." + (state ? "enabled" : "disabled"));
    }

    private static void toggleElytra() {
        elytraToggle = !elytraToggle;
        saveConfig();
        sendFeedback("msg.clientflightmod.elytra_toggle", elytraToggle ? "enabled" : "disabled");
    }

    private static void setSpeed(double value) {
        speed = clamp(value, 0.01, 5.0);
        saveConfig();
        sendFeedback("msg.clientflightmod.speed_set", speed);
    }

    private static void setMaxSpeed(double value) {
        maxSpeed = clamp(value, 0.1, 5.0);
        saveConfig();
        sendFeedback("msg.clientflightmod.maxspeed_set", maxSpeed);
    }

    private static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle) return;
        if (!player.getAbilities().allowFlying || !player.isGliding()) return;

        Vec3d velocity = player.getVelocity();
        double currentSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (currentSpeed >= maxSpeed) return;

        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;
        if (forward == 0 && sideways == 0) return;

        float yaw = (float) Math.toRadians(player.getYaw());
        Vec3d motion = new Vec3d(
                -MathHelper.sin(yaw) * forward + MathHelper.cos(yaw) * sideways,
                0,
                MathHelper.cos(yaw) * forward + MathHelper.sin(yaw) * sideways
        ).normalize().multiply(Math.min(speed, maxSpeed - currentSpeed));

        player.addVelocity(motion.x, motion.y, motion.z);
        player.velocityModified = true;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private static void sendFeedback(String key, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(key, args), true);
        }
    }
}