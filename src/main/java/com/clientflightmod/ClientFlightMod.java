package com.clientflightmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ClientFlightMod implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("cfly")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        boolean canFly = client.player.getAbilities().allowFlying;
                        
                        client.player.getAbilities().allowFlying = !canFly;
                        client.player.getAbilities().flying = !canFly;
                        
                        // 使用语言文件键
                        String translationKey = canFly ? 
                            "clientflightmod.fly.disabled" : 
                            "clientflightmod.fly.enabled";
                        
                        client.player.sendMessage(
                            Text.translatable(translationKey), 
                            true
                        );
                    }
                    return 1;
                })
            );
        });
    }
}