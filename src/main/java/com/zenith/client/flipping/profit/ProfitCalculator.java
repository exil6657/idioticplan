package com.zenith.client.flipping.profit;

import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.tax.TaxBadge;
import com.zenith.client.flipping.tax.TaxCalculator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central profit/ROI evaluator. Given a raw (itemId, buyPrice, sellPrice)
 * computes the net profit after AH tax, listing fee, and expected volume
 * adjustment, then attaches a confidence score (0..1) based on liquidity.
 */
public final class ProfitCalculator {

    private static final ProfitCalculator INSTANCE = new ProfitCalculator();
    public static ProfitCalculator getInstance() { return INSTANCE; }

    /** itemId → tax badge cache. Defaults to GENERAL. */
    private final Map<String, TaxBadge> badges = new ConcurrentHashMap<>();

    /** itemId → 24h volume estimate from Coflnet. */
    private final Map<String, Long> volumeCache = new ConcurrentHashMap<>();

    /** itemId → average 7d price. */
    private final Map<String, Long> avgCache = new ConcurrentHashMap<>();

    private ProfitCalculator() {}

    public void setBadge(String itemId, TaxBadge b) { badges.put(itemId, b); }

    public void setVolume(String itemId, long v) { volumeCache.put(itemId, Math.max(0L, v)); }
    public void setAverage(String itemId, long p) { avgCache.put(itemId, Math.max(0L, p)); }

    public long volume(String itemId) { return volumeCache.getOrDefault(itemId, 0L); }
    public long average(String itemId) { return avgCache.getOrDefault(itemId, -1L); }

    /**
     * Evaluate a raw candidate.
     *
     * @param itemId skyblock id
     * @param itemName display name
     * @param type flip type
     * @param buyPrice acquisition cost (after any buy-side fees)
     * @param sellPrice target listing price
     * @param count stack size
     * @param auctionUUID auction id (or "")
     * @param seller seller name (or "")
     * @param tier rarity tier
     * @return a fully-built FlipCandidate, or null if not worth pursuing.
     */
    public FlipCandidate evaluate(String itemId, String itemName, FlipType type,
                                  long buyPrice, long sellPrice, int count,
                                  String auctionUUID, String seller, String tier,
                                  long minProfit, double minPercentProfit) {
        TaxBadge badge = badges.getOrDefault(itemId.toUpperCase(), TaxBadge.GENERAL);
        long netPer = switch (type) {
            case AH_BIN, AH_BID, BOOK_APPLY, MUSEUM -> TaxCalculator.netFromBin(sellPrice, 1, badge.accessory);
            case CRAFT, NPC_RESELL -> TaxCalculator.netFromBin(sellPrice, 1, false);
            case BAZAAR_SPREAD -> TaxCalculator.bazaarInstantSellReceived(sellPrice, 1);
        };
        long profitPer = netPer - buyPrice;
        if (profitPer < minProfit) return null;
        double roi = buyPrice > 0 ? (double) profitPer / (double) buyPrice : 0d;
        if (roi < minPercentProfit) return null;

        // Confidence scales with volume — low volume = illiquid = risky flip.
        long v = volumeCache.getOrDefault(itemId, 0L);
        double confidence = confidenceFromVolume(v);

        // Expected hold time scales inversely with volume.
        long holdMs = holdTimeMs(v);

        return new FlipCandidate(itemId, itemName, type, buyPrice, sellPrice,
                profitPer, roi, v, confidence, holdMs, auctionUUID, seller, count, tier);
    }

    private double confidenceFromVolume(long v24h) {
        if (v24h <= 0) return 0.15d;
        // Saturates at ~200/day = 0.95.
        double c = 1d - Math.exp(-v24h / 80d);
        return Math.max(0.05d, Math.min(0.99d, c));
    }

    private long holdTimeMs(long v24h) {
        // Convert "per day" volume into expected time to sell one unit.
        if (v24h <= 0) return 4L * 60L * 60_000L; // 4 hours default
        double hours = 24d / Math.max(1d, v24h);
        return (long) Math.max(60_000L, hours * 3_600_000d);
    }
}
