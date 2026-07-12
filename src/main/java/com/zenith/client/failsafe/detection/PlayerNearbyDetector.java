package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.PlayerNearbyEvent;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Fires when another player is visible within the configured radius.
 *
 * <p>Two sources feed this detector:
 * <ol>
 *   <li>Chat messages from the server ("X has come close to you" — Hypixel's own proximity warning).</li>
 *   <li>Entity scanning: every tick we enumerate nearby players and flag if any is within 32 blocks of us.</li>
 * </ol>
 * Entity scanning is the primary catch — chat proximity messages are unreliable on SkyBlock
 * (e.g. during Jacob contests, in the Garden, etc.).</p>
 */
public class PlayerNearbyDetector extends AbstractDetector {

    private static final double ALERT_RADIUS = 32.0d;
    private long lastSeenNearbyMs;
    private String lastPlayerName;

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        if (mc.player == null || mc.level == null) { clear(FailsafeType.PLAYER_NEARBY); return; }

        String name = null;
        double closest = Double.MAX_VALUE;
        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            if (p.isSpectator()) continue;
            // Staff vanish: skip NPC-like "armor stand" names is a later phase.
            double dx = p.getX() - px, dy = p.getY() - py, dz = p.getZ() - pz;
            double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (d < ALERT_RADIUS && d < closest) {
                closest = d;
                name = p.getGameProfile().getName();
            }
        }
        if (name != null) {
            lastSeenNearbyMs = nowMs;
            lastPlayerName = name;
            trigger(FailsafeType.PLAYER_NEARBY, name + " is " + String.format("%.1f", closest) + "b away");
        } else {
            // If no entity nearby and the last chat sighting was >3s ago, clear.
            if ((nowMs - lastSeenNearbyMs) > 3000) clear(FailsafeType.PLAYER_NEARBY);
        }
    }

    @SubscribeEvent
    public void onChatNearby(PlayerNearbyEvent ev) {
        lastSeenNearbyMs = System.currentTimeMillis();
        lastPlayerName = ev.getPlayerName();
        trigger(FailsafeType.PLAYER_NEARBY, ev.getPlayerName() + " (chat) " + String.format("%.1fb", ev.getDistance()));
    }
}
