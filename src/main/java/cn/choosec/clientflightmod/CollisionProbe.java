package cn.choosec.clientflightmod;

//#if MC>=12104
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
//#endif

/** Keeps the server's collision state close to the client while flight is client-only. */
public final class CollisionProbe {
    private CollisionProbe() {}

    //#if MC>=12104
    private static Vec3 expectedPosition;
    private static float expectedPitch;
    private static int expectedTick = -100;
    private static int sequence;

    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!Flight.isClientFlying(player)) {
            clear();
            return;
        }
        if (expectedPosition != null) {
            if (player.tickCount - expectedTick > 40) clear();
            return;
        }
        if (player.tickCount - expectedTick < 2) return;

        Vec3 target = findCollisionTarget(player);
        if (target == null) return;

        expectedPosition = player.position();
        expectedTick = player.tickCount;
        expectedPitch = markerPitch(player.getXRot());

        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                player.getX(), player.getY(), player.getZ(), player.getYRot(), expectedPitch, false,
                player.horizontalCollision));
        player.connection.send(new ServerboundMovePlayerPacket.Pos(
                target.x, target.y, target.z, false, player.horizontalCollision));
    }

    public static boolean matches(PositionMoveRotation change, Set<Relative> relatives, Entity entity) {
        if (expectedPosition == null) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || entity != player || !relatives.isEmpty()) return false;
        return change.deltaMovement().lengthSqr() == 0
                && change.position().distanceToSqr(expectedPosition) < 1.0E-8
                && Float.floatToIntBits(change.xRot()) == Float.floatToIntBits(expectedPitch);
    }

    public static boolean receive(ClientboundPlayerPositionPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !matches(packet.change(), packet.relatives(), player)) {
            clear();
            return false;
        }

        clear();
        //#if MC>=260300
        //$$ player.connection.send(new ServerboundAcceptTeleportationPacket(packet.id(),
        //$$         player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
        //#else
        player.connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
        //#endif
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false, false));
        return true;
    }

    public static void finish() {
        clear();
    }

    public static void clear() {
        expectedPosition = null;
        expectedTick = -100;
    }

    private static float markerPitch(float pitch) {
        sequence++;
        float marker = pitch == 90.0F ? Math.nextDown(pitch) : Math.nextUp(pitch);
        for (int i = 0; i < (sequence & 63) + 1; i++) {
            marker = pitch >= 89.0F ? Math.nextDown(marker) : Math.nextUp(marker);
        }
        return marker;
    }

    private static Vec3 findCollisionTarget(LocalPlayer player) {
        Vec3 feet = player.position();
        var level = player.level();
        var floor = level.clip(new ClipContext(feet.add(0, 0.01, 0), feet.add(0, -8, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (floor.getType() == HitResult.Type.BLOCK) {
            Vec3 target = new Vec3(feet.x, floor.getLocation().y - 0.0625, feet.z);
            AABB movedBox = player.getBoundingBox().move(target.subtract(feet));
            if (!level.noCollision(player, movedBox)) return target;
        }

        Vec3 head = feet.add(0, player.getBbHeight(), 0);
        var ceiling = level.clip(new ClipContext(head, head.add(0, 8, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (ceiling.getType() == HitResult.Type.BLOCK) {
            Vec3 target = new Vec3(feet.x, ceiling.getLocation().y - player.getBbHeight() + 0.0625, feet.z);
            AABB movedBox = player.getBoundingBox().move(target.subtract(feet));
            if (!level.noCollision(player, movedBox)) return target;
        }

        Vec3 middle = feet.add(0, player.getBbHeight() / 2, 0);
        for (Direction direction : new Direction[] {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            var wall = level.clip(new ClipContext(middle,
                    middle.add(direction.getStepX() * 6, 0, direction.getStepZ() * 6),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (wall.getType() != HitResult.Type.BLOCK) continue;
            double margin = player.getBbWidth() / 2 - 0.0625;
            Vec3 target = new Vec3(
                    wall.getLocation().x + wall.getDirection().getStepX() * margin,
                    feet.y,
                    wall.getLocation().z + wall.getDirection().getStepZ() * margin);
            AABB movedBox = player.getBoundingBox().move(target.subtract(feet));
            if (!level.noCollision(player, movedBox)) return target;
        }
        return null;
    }
    //#endif
}
