package com.zenith.client.flipping.ah;

/**
 * Picks the right strategy for relisting: undercut by a fixed coin amount for
 * low-value items, percentage undercut for expensive items, or hold-steady if
 * we're already the cheapest.
 *
 * <p>Phase 9: thin wrapper around {@link RelistStrategy}; future phase adds
 * dynamic sizing based on observed velocity.</p>
 */
public final class AHListingOptimizer {

    private AHListingOptimizer() {}

    public static long optimalPrice(long paidCost, long currentLowestBin, long expectedSalePrice) {
        if (currentLowestBin <= 0) return expectedSalePrice;
        return BINListingStrategy.priceFor(new com.zenith.client.flipping.order.Order(
                new com.zenith.client.flipping.FlipCandidate("","", com.zenith.client.flipping.FlipType.AH_BIN,
                        paidCost, currentLowestBin, 0, 0, 0, 0, 0, "", "", 1, "")));
    }
}
