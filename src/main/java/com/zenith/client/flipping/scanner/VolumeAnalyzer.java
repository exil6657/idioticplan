package com.zenith.client.flipping.scanner;

import com.zenith.client.api.cache.BINCache;
import com.zenith.client.flipping.profit.ProfitCalculator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-item trade volume estimates, populated from Coflnet price-history
 * queries. Volume is smoothed with exponential decay so a one-off sale doesn't
 * mislead.
 */
public final class VolumeAnalyzer {

    private static final VolumeAnalyzer INSTANCE = new VolumeAnalyzer();
    public static VolumeAnalyzer getInstance() { return INSTANCE; }

    private final Map<String, Double> smoothVolume = new ConcurrentHashMap<>();
    private long lastUpdateMs;

    private VolumeAnalyzer() {}

    /** Update with a fresh 24h volume sample from Coflnet. */
    public void updateVolume(String itemId, long unitsPerDay) {
        if (itemId == null) return;
        double prev = smoothVolume.getOrDefault(itemId.toUpperCase(), 0d);
        double next = prev * 0.7d + Math.max(0d, unitsPerDay) * 0.3d;
        smoothVolume.put(itemId.toUpperCase(), next);
        ProfitCalculator.getInstance().setVolume(itemId, (long) next);
        lastUpdateMs = System.currentTimeMillis();
    }

    public double volume(String itemId) { return smoothVolume.getOrDefault(itemId.toUpperCase(), 0d); }
    public long lastUpdateAgeMs() { return System.currentTimeMillis() - lastUpdateMs; }
}
