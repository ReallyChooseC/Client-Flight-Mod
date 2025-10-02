package cn.choosec.clientflightmod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Feedback {
    static void sendFeedback(String key, Object... args) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.translatable(key, args), true);
        }
    }

    static void sendCustomFeedback(Component message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(message, true);
        }
    }
}