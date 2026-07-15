package com.zenith.client.flipping.break_;

import com.google.gson.annotations.Expose;

/**
 * Scheduled anti-pattern break configuration. To look human, the flipper must
 * pause periodically for several minutes — get up, walk around, look at chat.
 * The {@link BreakScheduler} inserts these breaks without stopping the bot
 * abruptly; sales already in progress are allowed to complete, but new orders
 * are paused.
 */
public class BreakConfig {
    @Expose public long playSessionMs = 45L * 60_000L;        // 45 min
    @Expose public long breakDurationMs = 5L * 60_000L;       // 5 min
    @Expose public double breakJitterSigma = 0.2d;            // ±20% jitter
    @Expose public boolean enabled = true;
}
