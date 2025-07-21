package cn.choosec.clientflightmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Feedback {
    static void sendFeedback(String key, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(key, args), true);
        }
    }

    static void sendCustomFeedback(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(message, true);
        }
    }
}
