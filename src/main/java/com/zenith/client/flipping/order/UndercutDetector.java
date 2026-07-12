package com.zenith.client.flipping.order;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.api.coflnet.CoflnetAuctionAPI;

/**
 * Watches listed orders and warns/relists if someone undercuts our listing.
 * Phase 9 only detects and logs; automatic relist is gated behind a config
 * flag in the polishing phase.
 */
public final class UndercutDetector {

    private static final UndercutDetector INSTANCE = new UndercutDetector();
    public static UndercutDetector getInstance() { return INSTANCE; }

    private boolean autoRelist = false;
    private long undercutThreshold = 1_000L; // ignore <1k undercuts

    private UndercutDetector() {}

    public void setAutoRelist(boolean v) { this.autoRelist = v; }

    public void check(Order o) {
        if (o.state != OrderState.LISTED) return;
        long bin = BINCache.getInstance().get(o.itemId());
        if (bin > 0 && bin < o.listPrice - undercutThreshold) {
            ZenithClient.LOGGER.info("[Undercut] {} listed @{} but lowest BIN is now {}",
                    o.itemId(), o.listPrice, bin);
            if (autoRelist) {
                // Future: cancel auction + relist at (bin - 1).
            }
        }
    }
}
