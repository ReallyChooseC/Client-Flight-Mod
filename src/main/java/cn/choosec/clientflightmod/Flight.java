package cn.choosec.clientflightmod;

import static cn.choosec.clientflightmod.ClientFlightMod.forceflightToggle;
import static cn.choosec.clientflightmod.Feedback.sendCustomFeedback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.lang.ref.WeakReference;

public class Flight {
    private static WeakReference<LocalPlayer> flightPlayer = new WeakReference<>(null);

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
        if (state) flightPlayer = new WeakReference<>(player);
        else flightPlayer.clear();
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
            if (!client.player.getAbilities().mayfly) flightPlayer = new WeakReference<>(client.player);
            client.player.getAbilities().mayfly = true;
        }
    }

    static void tickFlight() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.getAbilities().mayfly) flightPlayer.clear();
        if (forceflightToggle) forceFlight();
        //#if MC>=12104
        CollisionProbe.tick();
        //#endif
    }

    public static boolean isClientFlying(LocalPlayer player) {
        return player != null && flightPlayer.get() == player && player.getAbilities().mayfly && player.getAbilities().flying
                && !player.getAbilities().instabuild && !player.isSpectator()
                && !player.onGround() && !player.isPassenger() && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava() && !player.onClimbable();
    }
}
