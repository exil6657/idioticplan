package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.PacketReceiveEvent;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Detects unexpected player teleports (un-synced position changes that were
 * not the result of our own pathfinding movement).
 *
 * <p>Hypixel uses teleport packets for lags, staff pulls, /warp, /is,
 * being kicked from an island, etc. If the player moves more than ~3 blocks
 * horizontally or 5 blocks vertically within a single tick and we weren't
 * expecting it (e.g. via an etherwarp request), that's a trigger.</p>
 */
public class TeleportDetector extends AbstractDetector {

    private double lastX, lastY, lastZ;
    private boolean haveLast = false;
    private long lastTriggerMs;

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        Player p = mc.player;
        if (p == null) { haveLast = false; clear(FailsafeType.TELEPORT); return; }
        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (haveLast) {
            double dx = x - lastX, dy = y - lastY, dz = z - lastZ;
            double horiz = Math.hypot(dx, dz);
            // Ignore our own etherwarp: PathRequest with ETHERWARP mode sets this flag (future phase).
            boolean etherwarpExpected = false; // TODO: consult ZenithPath for etherwarp completion
            if ((horiz > 3.5d || Math.abs(dy) > 5d) && !etherwarpExpected) {
                if ((nowMs - lastTriggerMs) > 1500) {
                    lastTriggerMs = nowMs;
                    trigger(FailsafeType.TELEPORT, String.format("moved %.1fh / %.1fv blocks unexpectedly", horiz, dy));
                }
            } else {
                // Allow gravity drops of < 5 blocks without clearing (these are just falls).
                if (horiz < 0.5d && Math.abs(dy) < 0.5d) clear(FailsafeType.TELEPORT);
            }
        }
        lastX = x; lastY = y; lastZ = z; haveLast = true;
    }

    @SubscribeEvent
    public void onPacket(PacketReceiveEvent ev) {
        // Future: look for ClientboundPlayerPositionPacket, ClientboundTeleportEntityPacket targeting the player.
    }
}
