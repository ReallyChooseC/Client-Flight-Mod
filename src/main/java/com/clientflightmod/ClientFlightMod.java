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
import org.lwjgl.glfw.GLFW;

import java.io.File;

import static com.clientflightmod.Config.loadConfig;
import static com.clientflightmod.Elytra.*;
import static com.clientflightmod.Feedback.sendCustomFeedback;
import static com.clientflightmod.Nofall.*;

public class ClientFlightMod implements ClientModInitializer {
    private static KeyBinding flyKey;
    static final File CONFIG_FILE = new File("config/clientflight.cfg");
    static boolean elytraToggle = true;
    static boolean nofallToggle = true;
    static double speed = 1.0;
    static double nofallDistance = 3.0;
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

                )
                .then(ClientCommandManager.literal("nofallDistance")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                .executes(ctx -> { setNofallDistance(DoubleArgumentType.getDouble(ctx, "value")); return 1; }))

                )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (flyKey.wasPressed()) toggleFlight();
            handleElytraMovement(client);
            NofallDamage(client);
        });
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


}