package com.zenith.client.flipping.ah;

import com.zenith.client.flipping.tax.TaxCalculator;

/** Utility for choosing between BIN-sniping and bid-sniping an auction. */
public final class BINvsBidCalculator {

    private BINvsBidCalculator() {}

    /**
     * @param binPrice the BIN price
     * @param topBid current top bid on the non-BIN auction
     * @param remainingMs time remaining in the auction
     * @param listPrice the price we'd re-list at
     * @return true if the BIN is strictly better than bidding
     */
    public static boolean preferBin(long binPrice, long topBid, long remainingMs, long listPrice) {
        long binNet = TaxCalculator.netFromBin(listPrice) - binPrice;
        // Bidding carries a risk of being outbid at the last second; discount expected profit.
        long bidNet = (long) ((TaxCalculator.netFromBid(listPrice) - topBid) * bidConfidence(remainingMs));
        return binNet >= bidNet;
    }

    private static double bidConfidence(long remainingMs) {
        // If < 30 s remain, confidence is high; else it's a gamble.
        if (remainingMs < 30_000L) return 0.85d;
        if (remainingMs < 5 * 60_000L) return 0.6d;
        return 0.3d;
    }
}
