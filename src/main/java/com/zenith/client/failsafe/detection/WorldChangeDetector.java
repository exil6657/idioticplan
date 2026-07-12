package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.WorldChangeEvent;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;

/**
 * Pauses macros when the player switches worlds/islands/dimensions unless the
 * navigation system explicitly authorised the transition (e.g. a warp from
 * our own etherwarp or teleport command).
 */
public class WorldChangeDetector extends AbstractDetector {

    private volatile boolean fired = false;
    private long lastChangeMs;
    private String lastWorld;

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent ev) {
        fired = true;
        lastChangeMs = System.currentTimeMillis();
        lastWorld = ev.getWorldName();
        trigger(FailsafeType.WORLD_CHANGE, "joined " + ev.getWorldName());
    }

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        if (mc.player == null || mc.level == null) {
            fired = false;
            clear(FailsafeType.WORLD_CHANGE);
            return;
        }
        if (fired && (nowMs - lastChangeMs) > 8000) {
            // Allowed to clear if player has settled and no further triggers.
            fired = false;
            clear(FailsafeType.WORLD_CHANGE);
        }
    }
}
