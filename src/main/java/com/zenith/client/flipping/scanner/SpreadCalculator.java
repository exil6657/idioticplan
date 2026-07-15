package com.zenith.client.flipping.scanner;

import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.profit.ProfitCalculator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Given live BIN data (itemId → list of lowest prices), compute the spread
 * between the cheapest listing and the median/mean of the next few. Returns
 * FlipCandidate entries for items with sufficient spread after tax.
 *
 * <p>This is the core logic behind "instant BIN sniping" — we look for underpriced
 * listings relative to a robust estimate of the true market price.</p>
 */
public final class SpreadCalculator {

    private final Map<String, PriceHistory> histories = new ConcurrentHashMap<>();

    public void record(String itemId, long price) {
        histories.computeIfAbsent(itemId.toUpperCase(), k -> new PriceHistory(64)).add(price);
    }

    /**
     * @param itemId skyblock id
     * @param buyPrice the (cheap) listing we might buy
     * @param medianPrice median listing of that item
     * @param itemName display name
     * @param minProfit absolute minimum profit (coins)
     * @param minRoi minimum ROI ratio (e.g. 0.15 for 15%)
     */
    public FlipCandidate evaluateBin(String itemId, long buyPrice, long medianPrice,
                                     String itemName, int count, String auctionUUID,
                                     String seller, String tier,
                                     long minProfit, double minRoi) {
        // Record this listing in history regardless.
        record(itemId, buyPrice);
        if (medianPrice <= buyPrice) return null;
        return ProfitCalculator.getInstance().evaluate(
                itemId, itemName, FlipType.AH_BIN,
                buyPrice, medianPrice, count, auctionUUID, seller, tier,
                minProfit, minRoi);
    }

    public PriceHistory history(String itemId) {
        return histories.get(itemId.toUpperCase());
    }

    public void reset() { histories.clear(); }
}
