package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Detects lagback (rubber-banding) — the server resetting the player to a
 * previous position because the client moved too fast/illegally.
 *
 * <p>Signals: player position jumps "backwards" by more than ~0.8 blocks
 * horizontally between consecutive ticks while on ground (i.e. not falling,
 * not teleporting). Not definitive on its own — we fire a PAUSE so the
 * macro can stop and let the connection stabilise.</p>
 */
public class LagbackDetector extends AbstractDetector {

    private double lastX, lastY, lastZ;
    private boolean haveLast = false;
    private long lastFlagMs;

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { haveLast = false; clear(FailsafeType.LAGBACK); return; }
        double x = p.getX(), y = p.getY(), z = p.getZ();
        if (haveLast) {
            double dx = x - lastX, dz = z - lastZ;
            double horiz = Math.hypot(dx, dz);
            if (p.onGround() && horiz > 0.8d) {
                // Did we move backward relative to prior velocity? Compare signs in a very small way.
                if (nowMs - lastFlagMs > 2000) {
                    lastFlagMs = nowMs;
                    trigger(FailsafeType.LAGBACK, String.format("rubberband %.2fb", horiz));
                }
            }
        }
        lastX = x; lastY = y; lastZ = z; haveLast = true;
        if (nowMs - lastFlagMs > 2500) clear(FailsafeType.LAGBACK);
    }
}
