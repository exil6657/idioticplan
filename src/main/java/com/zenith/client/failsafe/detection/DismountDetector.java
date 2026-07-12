package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Fires NOTIFY when the player is dismounted from an entity (horse, pig,
 * minecart, boat, etc.) unexpectedly. Macros that rely on mounts call
 * {@link #expectedDismount()} before manually dismounting.</p>
 */
public class DismountDetector extends AbstractDetector {

    private static long expectedWindow = 0;

    private boolean wasRiding = false;
    private long lastDismountMs;

    public static void expectedDismount() { expectedWindow = System.currentTimeMillis() + 1200L; }

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { wasRiding = false; clear(FailsafeType.DISMOUNT); return; }
        boolean riding = p.isPassenger();
        if (wasRiding && !riding) {
            if (nowMs > expectedWindow) {
                lastDismountMs = nowMs;
                trigger(FailsafeType.DISMOUNT, "dismounted unexpectedly");
            }
            expectedWindow = 0;
        }
        wasRiding = riding;
        if (nowMs - lastDismountMs > 2000) clear(FailsafeType.DISMOUNT);
    }
}
