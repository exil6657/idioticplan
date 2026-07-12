package com.zenith.client.core.protection;

/**
 * Pluggable GUI-click hook for bits items.
 *
 * <p>Phase 2: interface only. Phase 6 (GUI) will install an implementation that
 * calls {@link BitsProtection#shouldBlockSpend(String, long, boolean)} before
 * forwarding InventoryClick-type packets.</p>
 */
public final class BitsSpendBlocker {

    private BitsSpendBlocker() {}

    /**
     * Pre-flight check called by inventory / shop click paths.
     *
     * @param featureId logical feature id (e.g. "experiment_serum")
     * @param costBits cost in bits
     * @param shiftHeld whether the player is holding shift
     * @return true if the click should be cancelled
     */
    public static boolean preClick(String featureId, long costBits, boolean shiftHeld) {
        return BitsProtection.getInstance().shouldBlockSpend(featureId, costBits, shiftHeld);
    }
}
