package com.clientflightmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ClientFlightMod implements ClientModInitializer {
    private static boolean flightEnabled = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("cfly")
                .executes(context -> {
                    flightEnabled = !flightEnabled;
                    updateFlightState();
                    context.getSource().sendFeedback(Text.literal("飞行模式 " + (flightEnabled ? "启用" : "禁用")));
                    return 1;
                });
        });
    }

    private static void updateFlightState() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.getAbilities().allowFlying = flightEnabled;
            if (!flightEnabled) {
                player.getAbilities().flying = false;
            }
        }
    }

	public static boolean isFlightEnabled() {
    	return flightEnabled;
	}
}