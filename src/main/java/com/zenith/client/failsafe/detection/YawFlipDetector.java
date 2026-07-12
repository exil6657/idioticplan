package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Catches sudden 180° yaw flips that look like a player turning to look at us
 * in a way that our own ZenithEyes wouldn't produce (e.g. Watchdog cinematic
 * camera, staff spectate teleport, free-look mods fighting us).
 *
 * <p>We track yaw between frames and look for >150° delta in one tick while
 * on ground (airborne flips are legitimate mouse movements during combat).</p>
 */
public class YawFlipDetector extends AbstractDetector {

    private float lastYaw;
    private boolean haveLast = false;
    private long lastFlagMs;

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { haveLast = false; clear(FailsafeType.YAW_FLIP); return; }
        float y = p.getYRot();
        if (haveLast) {
            float delta = Math.abs(angleDelta(y, lastYaw));
            if (delta > 150f && p.onGround()) {
                if (nowMs - lastFlagMs > 2000) {
                    lastFlagMs = nowMs;
                    trigger(FailsafeType.YAW_FLIP, String.format("%.0f° snap", delta));
                }
            }
        }
        lastYaw = y; haveLast = true;
        if (nowMs - lastFlagMs > 1500) clear(FailsafeType.YAW_FLIP);
    }

    private static float angleDelta(float a, float b) {
        float d = (a - b) % 360f;
        if (d > 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }
}
