package com.zenith.client.flipping;

/**
 * A proposed flip. Constructed by {@code MarketScanner} / strategy engines and
 * validated by ProfitCalculator + ItemFilter before being offered to the
 * order manager.
 */
public final class FlipCandidate {

    public final String itemId;            // SkyBlock id e.g. "ASPECT_OF_THE_END"
    public final String itemName;          // Display name (for UI)
    public final FlipType type;
    public final long buyPrice;            // coins we'd pay to acquire
    public final long sellPrice;           // coins we expect to list for
    public final long expectedProfit;      // sell - buy - fees
    public final double profitPercent;     // expectedProfit / buyPrice (for ROI)
    public final long volumePerDay;        // estimated traded units/day (liquidity)
    public final double sellConfidence;    // 0..1 — how likely the item sells within expected time
    public final long targetTimeMs;        // expected holding period
    public final String auctionUUID;       // for AH flips: auction id; else ""
    public final String seller;            // seller name (if known)
    public final int count;                // stack size
    public final String tier;              // rarity tier (UNCOMMON/RARE/EPIC/...)
    public final long foundAtMs;

    public FlipCandidate(String itemId, String itemName, FlipType type,
                         long buyPrice, long sellPrice, long expectedProfit, double profitPercent,
                         long volumePerDay, double sellConfidence, long targetTimeMs,
                         String auctionUUID, String seller, int count, String tier) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.type = type;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.expectedProfit = expectedProfit;
        this.profitPercent = profitPercent;
        this.volumePerDay = volumePerDay;
        this.sellConfidence = sellConfidence;
        this.targetTimeMs = targetTimeMs;
        this.auctionUUID = auctionUUID == null ? "" : auctionUUID;
        this.seller = seller == null ? "" : seller;
        this.count = Math.max(1, count);
        this.tier = tier == null ? "" : tier;
        this.foundAtMs = System.currentTimeMillis();
    }

    /** @return coins-per-hour estimate (linear, no compounding). */
    public double coinsPerHour() {
        if (targetTimeMs <= 0) return 0d;
        return (expectedProfit * 3_600_000d) / targetTimeMs;
    }

    @Override
    public String toString() {
        return "FlipCandidate{" + itemId + " buy=" + buyPrice + " sell=" + sellPrice +
                " profit=" + expectedProfit + " (" + (profitPercent*100) + "%) type=" + type + "}";
    }
}
