package com.zenith.client.failsafe.detection;

import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;

/**
 * Watches for rotation "snapback" — when the server (or another mod) resets
 * the player's yaw/pitch to values far from what ZenithEyes expects.
 *
 * <p>Snapbacks are a classic sign of rotation detection by anti-cheat — the
 * server rejected our look packet and reset us. When this fires we cancel any
 * pending rotation and emit a low-severity notification.</p>
 */
public class RotationDetector extends AbstractDetector {

    private long lastFlagMs;

    @Override
    public void tick(long nowMs) {
        var p = mc().player;
        if (p == null) { clear(FailsafeType.ROTATION_RESET); return; }
        float realYaw = p.getYRot();
        float realPitch = p.getXRot();
        var eyes = ZenithEyes.getInstance();
        var dbg = eyes.debugData();
        float expectedYaw = dbg.currentYaw;
        float expectedPitch = dbg.currentPitch;
        float yawDelta = Math.abs(angleDelta(realYaw, expectedYaw));
        float pitchDelta = Math.abs(realPitch - expectedPitch);
        if (yawDelta > 60f || pitchDelta > 45f) {
            if (nowMs - lastFlagMs > 1500) {
                lastFlagMs = nowMs;
                trigger(FailsafeType.ROTATION_RESET, String.format("yawΔ=%.1f° pitchΔ=%.1f°", yawDelta, pitchDelta));
                // Clear the rotation queue — our expectation is now wrong.
                ZenithEyes.getInstance().setEnabled(false);
            }
        } else {
            if (nowMs - lastFlagMs > 1500) clear(FailsafeType.ROTATION_RESET);
        }
    }

    private static float angleDelta(float a, float b) {
        float d = (a - b) % 360f;
        if (d > 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }
}
