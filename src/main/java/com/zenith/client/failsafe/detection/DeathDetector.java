package com.zenith.client.failsafe.detection;

import com.zenith.client.core.player.PlayerStateDetector;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/**
 * Fires PAUSE when the player dies. Macros must never click the respawn button
 * automatically on Hypixel (it's a bannable macro indicator) — we just stop
 * everything and notify the player.</p>
 */
public class DeathDetector extends AbstractDetector {

    private boolean deadLastTick = false;

    @Override
    public void tick(long nowMs) {
        var p = mc().player;
        boolean dead = PlayerStateDetector.getInstance().isDead() || mc().screen instanceof DeathScreen;
        if (dead && !deadLastTick) {
            trigger(FailsafeType.DEATH, "player died");
        }
        if (!dead && deadLastTick) {
            clear(FailsafeType.DEATH);
        }
        deadLastTick = dead;
    }
}
