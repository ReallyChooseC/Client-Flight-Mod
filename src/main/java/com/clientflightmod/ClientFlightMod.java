package com.clientflightmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClientFlightMod implements ClientModInitializer {
    private static KeyBinding flyKey;

    @Override
    public void onInitializeClient() {
        flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.clientflightmod.toggleflight",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.clientflightmod.main"
        ));

        ClientCommandRegistrationCallback.EVENT.register((d, r) -> d.register(ClientCommandManager.literal("cfly").executes(c -> {
            toggle();
            return 1;
        })));

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (flyKey.wasPressed()) toggle();
        });
    }

    private static void toggle() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null) return;
        boolean state = !c.player.getAbilities().allowFlying;
        c.player.getAbilities().allowFlying = state;
        c.player.sendMessage(Text.translatable("clientflightmod.fly." + (state ? "enabled" : "disabled")), true);
    }
}