package com.clientflightmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;

import static com.clientflightmod.ClientFlightMod.nofallDistance;
import static com.clientflightmod.ClientFlightMod.nofallToggle;
import static com.clientflightmod.Config.saveConfig;
import static com.clientflightmod.Feedback.*;

public class Nofall {
    static void NofallDamage(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        if (nofallToggle|!player.isGliding()) {
            if(player.fallDistance > nofallDistance) {
                player.networkHandler
                        .sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, player.horizontalCollision));
            }
        }
    }

    static void toggleNofall() {
        nofallToggle = !nofallToggle;
        saveConfig();
        Text message = Text.translatable("clientflightmod.nofall_toggle")
                .append(Text.translatable(": "))
                .append(Text.translatable("clientflightmod." + (nofallToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }

    static void setNofallDistance(double value) {
        nofallDistance = Math.max(0, value);
        saveConfig();
        sendFeedback("clientflightmod.nofallDistance_set", nofallDistance);
    }

}