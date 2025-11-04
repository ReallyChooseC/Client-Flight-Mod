package cn.choosec.clientflightmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import static cn.choosec.clientflightmod.ClientFlightMod.forceflightToggle;
import static cn.choosec.clientflightmod.Feedback.sendCustomFeedback;

public class Flight {
    static void toggleFlight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (forceflightToggle) {
            Text message = Text.translatable("clientflightmod.forceflight_enabled_warning");
            sendCustomFeedback(message);
            return;
        }

        ClientPlayerEntity player = client.player;
        boolean state = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = state;
        if (!state) player.getAbilities().flying = false;
        String statusKey = "clientflightmod." + (state ? "enabled" : "disabled");
        Text message = Text.translatable("clientflightmod.fly")
                .append(Text.literal(": "))
                .append(Text.translatable(statusKey));
        sendCustomFeedback(message);
    }

    static void toggleForceFlight() {
        forceflightToggle = !forceflightToggle;
        Config.saveConfig();
        Text message = Text.translatable("clientflightmod.forceflight_toggle")
                .append(Text.literal(": "))
                .append(Text.translatable("clientflightmod." + (forceflightToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);

        if (forceflightToggle) {
            forceFlight();
        }
    }

    static void forceFlight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.getAbilities().allowFlying = true;
        }
    }
}
