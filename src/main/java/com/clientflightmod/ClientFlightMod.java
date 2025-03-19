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
    public static double speed = 1.0;
    private static final double BASE_TWEAKEROO = 0.064;
    private static final double SCALE_FACTOR = 0.703;
    private static final double VERTICAL_RATIO = 0.689;
    private static final String TWEAKEROO_CONFIGS = "fi.dy.masa.tweakeroo.config.Configs";
    private static final String TWEAKEROO_FEATURES = "fi.dy.masa.tweakeroo.config.FeatureToggle";

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
                .then(ClientCommandManager.literal("speed")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                .executes(ctx -> { setSpeed(DoubleArgumentType.getDouble(ctx, "value")); return 1; }))
                )));

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

    private static void handleElytraMovement(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !elytraToggle || !player.getAbilities().allowFlying || !player.isFallFlying()) return;
