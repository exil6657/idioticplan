package com.zenith.client.flipping.order;

import com.zenith.client.api.cache.BINCache;
import com.zenith.client.flipping.tax.TaxCalculator;

/**
 * Picks a competitive list price when relisting an item. The default strategy
 * undercuts the current lowest BIN by a small randomised amount (subject to
 * our minimum profit floor).
 */
public final class RelistStrategy {

    private static final RelistStrategy INSTANCE = new RelistStrategy();
    public static RelistStrategy getInstance() { return INSTANCE; }

    private double undercutPercent = 0.005d; // 0.5%
    private long undercutFloor = 500L;
    private long undercutCap = 250_000L;
    private long minProfitFloor = 5_000L;

    private RelistStrategy() {}

    public long computeListPrice(Order o) {
        long lowBin = BINCache.getInstance().get(o.itemId());
        long paid = o.buyPrice;
        long desiredList = o.listPrice;
        long target;
        if (lowBin <= 0) {
            target = desiredList;
        } else {
            long undercut = Math.max(undercutFloor, Math.min(undercutCap,
                    (long) Math.ceil(lowBin * undercutPercent)));
            target = lowBin - undercut;
        }
        // Clamp so we never list below tax-adjusted cost + min profit.
        long breakEven = TaxCalculator.listPriceForNet(paid + minProfitFloor, false);
        return Math.max(breakEven, target);
    }
}
