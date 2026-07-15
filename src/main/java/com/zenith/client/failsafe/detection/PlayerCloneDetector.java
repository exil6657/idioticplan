package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.PacketReceiveEvent;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;

/**
 * Watches for {@code ClientboundPlayerInfoRemovePacket} / respawn packets that
 * indicate the player was cloned by the server (respawned, switched dimension
 * by force). On Hypixel this happens when you die and respawn, or are
 * transferred to limbo.
 *
 * <p>We treat a clone as a potential "server moved you" event and fire
 * {@link FailsafeType#TELEPORT}.</p>
 */
public class PlayerCloneDetector extends AbstractDetector {

    private int respawnCount = 0;
    private long lastCloneMs;

    @Override
    public void tick(long nowMs) {
        if (nowMs - lastCloneMs > 2000) clear(FailsafeType.TELEPORT);
    }

    @SubscribeEvent
    public void onPacket(PacketReceiveEvent ev) {
        Object pkt = ev.getPacket();
        if (pkt == null) return;
        String cn = pkt.getClass().getSimpleName();
        // 26.1 packets indicating a server-initiated state change:
        //   ClientboundRespawnPacket     — player respawned / dimension change
        //   ClientboundPlayerPositionPacket — server-corrected position (teleport/lagback)
        //   ClientboundLoginPacket       — fire-and-forget on join (ignored after first 2 s)
        // ClientboundLoginPacket only fires once at join, so guard the first 2 s to avoid a
        // spurious trigger immediately on login.
        boolean isLogin = "ClientboundLoginPacket".equals(cn);
        if ("ClientboundRespawnPacket".equals(cn)
                || "ClientboundPlayerPositionPacket".equals(cn)
                || (isLogin && System.currentTimeMillis() - lastCloneMs > 2000)) {
            respawnCount++;
            lastCloneMs = System.currentTimeMillis();
            if (!isLogin) {
                trigger(FailsafeType.TELEPORT, "server move: " + cn);
            }
        }
    }
}
