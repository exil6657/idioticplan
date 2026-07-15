package com.zenith.client.core.protection;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pluggable GUI-click hook for bits items.
 *
 * <p>Every click that would spend bits (and, during a failsafe, <em>every</em>
 * inventory click) goes through {@link #preClick(String, long, boolean)}. If
 * the global "block all" flag is set (by the failsafe manager), all clicks are
 * blocked unconditionally.</p>
 */
public final class BitsSpendBlocker {

    private static final AtomicBoolean BLOCK_EVERYTHING = new AtomicBoolean(false);

    private BitsSpendBlocker() {}

    /**
     * Enable/disable global click blocking. When enabled, {@link #preClick}
     * returns {@code true} for every invocation, blocking all outgoing
     * inventory-click packets. Used by the failsafe manager to prevent
     * automated clicks during an emergency.
     */
    public static void setBlocked(boolean v) { BLOCK_EVERYTHING.set(v); }

    public static boolean isBlocked() { return BLOCK_EVERYTHING.get(); }

    /**
     * Pre-flight check called by inventory / shop click paths.
     *
     * @param featureId logical feature id (e.g. "experiment_serum")
     * @param costBits cost in bits
     * @param shiftHeld whether the player is holding shift
     * @return true if the click should be cancelled
     */
    public static boolean preClick(String featureId, long costBits, boolean shiftHeld) {
        if (BLOCK_EVERYTHING.get()) return true;
        return BitsProtection.getInstance().shouldBlockSpend(featureId, costBits, shiftHeld);
    }
}
