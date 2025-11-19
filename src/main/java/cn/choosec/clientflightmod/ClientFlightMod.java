package cn.choosec.clientflightmod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
//#if MC>=12109
//$$ import net.minecraft.client.KeyMapping.Category;
//$$ import net.minecraft.resources.ResourceLocation;
//#endif
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static cn.choosec.clientflightmod.Config.loadConfig;
import static cn.choosec.clientflightmod.Elytra.*;
import static cn.choosec.clientflightmod.Flight.*;
import static cn.choosec.clientflightmod.Nofall.*;

public class ClientFlightMod implements ClientModInitializer {
    public static final String MOD_ID = "client-flight-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyMapping flyKey;
    static final File CONFIG_FILE = new File("config/clientflight.properties");
    public static boolean elytraToggle = true;
    public static boolean nofallToggle = true;
    static double speed = 1.0;
    static boolean forceflightToggle = false;
    static final double BASE_TWEAKEROO = 0.064;
    static final double SCALE_FACTOR = 0.703;
    public static final double VERTICAL_RATIO = 0.689;

    @Override
    public void onInitializeClient() {
        loadConfig();
        ReflectionCache.initialize();
        //#if MC<12109
        flyKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.clientflightmod.toggleflight",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.clientflightmod.main"));
        //#else
        //$$ Category clientflightmodCategory = KeyMapping.Category
        //$$         .register(ResourceLocation.fromNamespaceAndPath("clientflightmod", "main"));
        //$$ flyKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //$$         "key.clientflightmod.toggleflight",
        //$$         InputConstants.Type.KEYSYM,
        //$$         GLFW.GLFW_KEY_UNKNOWN,
        //$$         clientflightmodCategory));
        //#endif

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("cfly")
                .then(ClientCommandManager.literal("toggle").executes(ctx -> { toggleFlight(); return 1; }))
                .then(ClientCommandManager.literal("elytratoggle").executes(ctx -> { toggleElytra(); return 1; }))
                .then(ClientCommandManager.literal("nofalltoggle").executes(ctx -> { toggleNofall(); return 1; }))
                .then(ClientCommandManager.literal("forceflighttoggle").executes(ctx -> { toggleForceFlight(); return 1; }))
                .then(ClientCommandManager.literal("speed")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                .executes(ctx -> { setSpeed(DoubleArgumentType.getDouble(ctx, "value")); return 1; }))
                )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (flyKey.consumeClick()) toggleFlight();
            if (nofallToggle) noFallDamage(client);
            if (forceflightToggle) forceFlight();
        });
    }
}