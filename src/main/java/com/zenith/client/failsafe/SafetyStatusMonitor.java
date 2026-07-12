package com.zenith.client.failsafe;

/**
 * Tracks the "is anything wrong?" aggregate used by the HUD status indicator
 * and the F6 debug panel. Keeps a short history of the highest severity in the
 * last few seconds so transient one-tick blips don't cause indicator flicker.
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

    /** Colour for the HUD dot — matches ChatLevel palette roughly. */
    public int statusArgb() {
        return switch (displaySeverity) {
            case NONE        -> 0xFF34D399; // green
            case NOTIFY      -> 0xFFFBBF24; // amber
            case PAUSE, WARP_HOME, WARP_SPAWN -> 0xFFF97316; // orange
            case DISCONNECT  -> 0xFFEF4444; // red
        };
    }
}
