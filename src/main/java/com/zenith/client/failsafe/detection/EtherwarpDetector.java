package com.zenith.client.failsafe.detection;

import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Monitors Etherwarp usage. If an etherwarp was <em>not</em> issued by the
 * pathfinding engine yet the player teleports through a block (enderman-like
 * displacement), that means either a glitch, lag, or an external mod — we
 * pause.</p>
 */
public class EtherwarpDetector extends AbstractDetector {

    private long lastFlagMs;

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { clear(FailsafeType.TELEPORT); return; }
        // Etherwarp success sets a flag via ZenithPath (future). For now, no-op
        // besides clearing — main etherwarp sanity is handled by TeleportDetector.
        if (nowMs - lastFlagMs > 2000) clear(FailsafeType.TELEPORT);
    }
}
