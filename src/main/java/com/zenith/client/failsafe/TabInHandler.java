package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;

/**
 * Monitors window-focus events. If the player tabs out while a macro is active
 * and failsafes are in STRICT mode, we automatically PAUSE because the player
 * can't react to chat, players nearby, or staff messages.
 *
 * <p>Tabbing back in does NOT auto-resume (prevents "I tabbed back and it
 * immediately got me banned" class of incidents).</p>
 */
public final class TabInHandler {

    private final FailsafeManager mgr;
    private boolean wasFocused = true;
    private long lostFocusAtMs;

    TabInHandler(FailsafeManager mgr) { this.mgr = mgr; }

    public void init() {
        // No event for window focus yet — poll from the manager tick via check().
    }

    public void tick(long nowMs) {
        Minecraft mc = Minecraft.getInstance();
        boolean focused = mc.isWindowActive();
        if (wasFocused && !focused) {
            lostFocusAtMs = nowMs;
            // Only pause if any macro is active (macrosPaused tells us we were running).
            // We use "failsafe not currently active" as a proxy that macros were running.
            if (!mgr.areMacrosPaused()) {
                mgr.trigger(FailsafeType.CUSTOM, "lost window focus", FailsafeStrictness.PAUSE);
                ZenithClient.LOGGER.info("[Failsafe] Window lost focus — pausing.");
            }
        }
        wasFocused = focused;
    }

    public boolean wasFocusLost() { return !wasFocused; }
    public long lostFocusAtMs() { return lostFocusAtMs; }
}
