package com.zenith.client.core.protection;

import com.zenith.client.core.chat.ZenithChat;

/**
 * Special-case blocker for the Experiment Serum (expensive bits item that
 * players accidentally click during experimentation table sessions).
 */
public final class ExperimentSerumBlocker {

    /** Feature id used in BitsConfig.blockList. */
    public static final String FEATURE_ID = "experiment_serum";

    private ExperimentSerumBlocker() {}

    public static boolean blockIfSerum(String inventoryTitle, String slotId, long costBits, boolean shiftHeld) {
        if (inventoryTitle == null || slotId == null) return false;
        if (inventoryTitle.contains("Experiment") && slotId.toLowerCase().contains("serum")) {
            if (BitsProtection.getInstance().shouldBlockSpend(FEATURE_ID, costBits, shiftHeld)) {
                ZenithChat.getInstance().warn("Blocked Experiment Serum purchase ({} bits). Hold SHIFT to override.", costBits);
                return true;
            }
        }
        return false;
    }
}
