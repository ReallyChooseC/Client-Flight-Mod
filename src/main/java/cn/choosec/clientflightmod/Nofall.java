package cn.choosec.clientflightmod;

import static cn.choosec.clientflightmod.ClientFlightMod.nofallToggle;
import static cn.choosec.clientflightmod.Config.saveConfig;
import static cn.choosec.clientflightmod.Feedback.*;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Nofall {
    private static final double LANDING_RESET_OFFSET = 0.01;

    public static void prepareSafeLanding(LocalPlayer player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // Preserve the final real descent, reset the server's fall distance with
        // a tiny upward step, then finish at the exact landing position.
        sendPosition(player, x, y, z, false);
        sendPosition(player, x, y + LANDING_RESET_OFFSET, z, false);
        sendPosition(player, x, y, z, true);
    }

    private static void sendPosition(LocalPlayer player, double x, double y, double z, boolean onGround) {
        player.connection.send(new ServerboundMovePlayerPacket.Pos(
                x, y, z, onGround
                //#if MC>12101
                , player.horizontalCollision
                //#endif
        ));
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
