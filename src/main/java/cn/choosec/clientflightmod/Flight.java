package cn.choosec.clientflightmod;

import static cn.choosec.clientflightmod.ClientFlightMod.forceflightToggle;
import static cn.choosec.clientflightmod.Feedback.sendCustomFeedback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class Flight {
    static void toggleFlight() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (forceflightToggle) {
            Component message = Component.translatable("clientflightmod.forceflight_enabled_warning");
            sendCustomFeedback(message);
            return;
        }

        LocalPlayer player = client.player;
        boolean state = !player.getAbilities().mayfly;
        player.getAbilities().mayfly = state;
        if (!state) player.getAbilities().flying = false;
        String statusKey = "clientflightmod." + (state ? "enabled" : "disabled");
        Component message = Component.translatable("clientflightmod.fly")
                .append(Component.literal(": "))
                .append(Component.translatable(statusKey));
        sendCustomFeedback(message);
    }

    static void toggleForceFlight() {
        forceflightToggle = !forceflightToggle;
        Config.saveConfig();
        Component message = Component.translatable("clientflightmod.forceflight_toggle")
                .append(Component.literal(": "))
                .append(Component.translatable("clientflightmod." + (forceflightToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);

        if (forceflightToggle) {
            forceFlight();
        }
    }

    static void forceFlight() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.getAbilities().mayfly = true;
        }
    }
}
