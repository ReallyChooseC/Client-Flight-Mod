package cn.choosec.clientflightmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.chat.Component;

import static cn.choosec.clientflightmod.ClientFlightMod.nofallToggle;
import static cn.choosec.clientflightmod.Config.saveConfig;
import static cn.choosec.clientflightmod.Feedback.*;

public class Nofall {
    static void NofallDamage(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !nofallToggle || player.isFallFlying()) return;

        // 使用 StatusOnly 子类，它只发送 onGround 状态
        player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true));
    }

    static void toggleNofall() {
        nofallToggle = !nofallToggle;
        saveConfig();
        Component message = Component.translatable("clientflightmod.nofall_toggle")
                .append(Component.literal(": "))
                .append(Component.translatable("clientflightmod." + (nofallToggle ? "enabled" : "disabled")));
        sendCustomFeedback(message);
    }
}