package cn.choosec.clientflightmod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Feedback {
    static void sendFeedback(String key, Object... args) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            //#if MC>=260000
            //$$ client.player.sendOverlayMessage(Component.translatable(key, args));
            //#else
            client.player.displayClientMessage(Component.translatable(key, args), true);
            //#endif
        }
    }

    static void sendCustomFeedback(Component message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            //#if MC>=260000
            //$$ client.player.sendOverlayMessage(message);
            //#else
            client.player.displayClientMessage(message, true);
            //#endif
        }
    }
}
