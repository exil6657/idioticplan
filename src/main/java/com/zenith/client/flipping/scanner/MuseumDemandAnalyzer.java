package com.zenith.client.flipping.scanner;

/**
 * MuseumDemandAnalyzer — estimates demand from museum-donatable items.
 * Phase 9 stub: returns 0 for all items. Future phase reads museum data from
 * the NEU constants and boosts items missing from many player museums.
 */
public final class MuseumDemandAnalyzer {
    private static final MuseumDemandAnalyzer INSTANCE = new MuseumDemandAnalyzer();
    public static MuseumDemandAnalyzer getInstance() { return INSTANCE; }
    private MuseumDemandAnalyzer() {}
    public double demandMultiplier(String itemId) { return 1.0d; }
}
