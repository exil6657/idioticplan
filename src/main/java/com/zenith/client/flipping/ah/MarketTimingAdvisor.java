package com.zenith.client.flipping.ah;

/**
 * Crude advisor for "good" flipping periods based on hourly-of-day (EU/US peaks).
 * Later phases will pull Jacob/Mayor events into the heuristic; Phase 9 only
 * uses a simple hourly profile.
 */
public final class MarketTimingAdvisor {

    private static final MarketTimingAdvisor INSTANCE = new MarketTimingAdvisor();
    public static MarketTimingAdvisor getInstance() { return INSTANCE; }

    private MarketTimingAdvisor() {}

    /**
     * @return a 0..1 multiplier for candidate profit threshold. Low = market is
     * slow, don't bother with thin margins; high = demand is up, accept more flips.
     */
    public double marketMultiplier() {
        var z = java.time.ZonedDateTime.now(java.time.ZoneId.of("Europe/London"));
        int h = z.getHour();
        // Peak EU evening 18-22, US evening 00-04 UTC.
        boolean euPeak = h >= 18 && h <= 22;
        boolean usPeak = h >= 0 && h <= 4;
        boolean low = h >= 5 && h <= 10;
        if (euPeak || usPeak) return 1.0d;
        if (low) return 0.6d;
        return 0.85d;
    }
}
