package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Detects "velocity kick" style anti-cheat flags.
 *
 * <p>Hypixel's Watchdog and NCP will knock a player back (apply large velocity)
 * when they suspect movement hacking. We detect sudden anomalous velocity that
 * the player themselves didn't cause (no movement input, airborne, large vy or
 * vx/vz spike).</p>
 */
public class VelocityDetector extends AbstractDetector {

    private double lastVx, lastVy, lastVz;
    private long lastFlagMs;

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { clear(FailsafeType.VELOCITY_KICK); return; }
        double vx = p.getDeltaMovement().x;
        double vy = p.getDeltaMovement().y;
        double vz = p.getDeltaMovement().z;

        // Suspicion: vertical velocity > 3 (near-tp up) OR horizontal > 1.5 mid-air when not jumping under our control.
        boolean suspicious = Math.abs(vy) > 3.0d || (Math.hypot(vx, vz) > 1.8d && !p.onGround());
        if (suspicious) {
            if ((nowMs - lastFlagMs) > 1000) {
                lastFlagMs = nowMs;
                trigger(FailsafeType.VELOCITY_KICK, String.format("v=(%.2f,%.2f,%.2f) onGround=%s",
                        vx, vy, vz, p.onGround()));
            }
        } else {
            if ((nowMs - lastFlagMs) > 2000) clear(FailsafeType.VELOCITY_KICK);
        }

        lastVx = vx; lastVy = vy; lastVz = vz;
    }
}
