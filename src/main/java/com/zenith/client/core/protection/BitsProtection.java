package com.zenith.client.core.protection;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;

/**
 * Master controller for bits-spend protection.
 *
 * <p>Phase 2: state/config only. The GUI click interception is wired in
 * Phase 6 via {@link BitsSpendBlocker}.</p>
 */
public final class BitsProtection {

    private static BitsProtection instance;

    private final BitsConfig config = new BitsConfig();
    private int blocksThisSession = 0;

    private BitsProtection() {}

    public static BitsProtection getInstance() {
        if (instance == null) instance = new BitsProtection();
        return instance;
    }

    public void init() {
        ZenithClient.LOGGER.info("[BitsProtection] Enabled (threshold={})", config.perClickThreshold);
    }

    public BitsConfig getConfig() { return config; }

    /** Called by GUI hooks when a bits-spend is about to occur. @return true to block. */
    public boolean shouldBlockSpend(String featureId, long cost, boolean shiftHeld) {
        if (!config.enabled) return false;
        if (config.blockList.contains(featureId)) {
            return block(featureId, cost, "blocklist");
        }
        if (cost > config.perClickThreshold) {
            if (config.requireShiftForSpend && !shiftHeld) {
                return block(featureId, cost, "exceeds threshold and shift not held");
            }
        }
        return false;
    }

    private boolean block(String featureId, long cost, String reason) {
        blocksThisSession++;
        ZenithChat.getInstance().warn("Blocked bits spend: {} ({} bits) — {}", featureId, cost, reason);
        return true;
    }

    public int getBlocksThisSession() { return blocksThisSession; }
}
