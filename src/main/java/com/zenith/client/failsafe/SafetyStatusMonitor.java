package com.zenith.client.failsafe;

/**
 * Tracks the "is anything wrong?" aggregate used by the HUD status indicator
 * and the F6 debug panel. Keeps a short history of the highest severity in the
 * last few seconds so transient one-tick blips don't cause indicator flicker.
 *
 * <p>Colour mapping (revised ladder): green = clear, amber = advisory/wiggle/
 * combat/obstruction/respawn/repath, orange = paused or warping, red = disconnect.</p>
 */
public final class SafetyStatusMonitor {

    private final FailsafeManager mgr;
    private FailsafeStrictness displaySeverity = FailsafeStrictness.NONE;
    private long lastActiveAt;

    public SafetyStatusMonitor(FailsafeManager mgr) { this.mgr = mgr; }

    public void tick(long nowMs) {
        FailsafeStrictness real = mgr.highestSeverity();
        if (real.level() > displaySeverity.level()) {
            displaySeverity = real;
            lastActiveAt = nowMs;
        } else if ((nowMs - lastActiveAt) > 1500) {
            displaySeverity = real;
        }
        mgr.sounds().tick(nowMs);
        mgr.notifier().tick(nowMs);
    }

    public FailsafeStrictness displaySeverity() { return displaySeverity; }

    /** Colour for the HUD dot. */
    public int statusArgb() {
        return switch (displaySeverity) {
            case NONE                                            -> 0xFF34D399; // green
            case NOTIFY, WIGGLE_REACT, COMBAT, REMOVE_OBSTRUCTION,
                 INSTANT_RESPAWN, REPATH                         -> 0xFFFBBF24; // amber — reaction in progress
            case PAUSE, WARP_ISLAND, WARP_HUB                    -> 0xFFF97316; // orange — paused/fleeing
            case DISCONNECT                                      -> 0xFFEF4444; // red
        };
    }
}
