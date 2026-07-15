package com.zenith.client.api.ratelimit;

/**
 * Default rate limits per endpoint. Conservative so we never look like a
 * scraper. Flip-finder needs tighter limits than wiki/constants.
 */
public final class RateLimitConfig {

    public static void applyDefaults(RateLimiter rl) {
        // Coflnet flip feed: once every 1.5 s.
        rl.configure("coflnet.flips", 1.0d / 1.5d, 60_000L);
        // Auction/bazaar price lookup: 2 req/s.
        rl.configure("coflnet.prices", 2.0d, 30_000L);
        // NEU repo: once per session (can be 1/min for periodic refresh).
        rl.configure("neu.repo", 1.0d / 60d, 120_000L);
        // NEU constants / items / recipes share the same repo; 1/min.
        rl.configure("neu.items", 1.0d / 60d, 120_000L);
        // Moulberry BIN: 1 req/10s (very slow-changing, cached).
        rl.configure("moulberry.bin", 1.0d / 10d, 60_000L);
        // Update check: once per launch.
        rl.configure("update.check", 1.0d / 3_600d, 600_000L);
    }

    private RateLimitConfig() {}
}
