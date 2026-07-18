package com.zenith.client.flipping.tax;

/**
 * AH tax brackets on Hypixel SkyBlock — verified 2026-01 (wiki.hypixel.net).
 *
 * <ul>
 *   <li>AH: 1% listing tax is actually the sale cut — 1% for most items.
 *       Accessory tax used to be 3% in older versions, now 1% as well but we keep
 *       a separate tier for future-proofing.</li>
 *   <li>Bazaar: 1.25% tax on both instant buy and instant sell (coins spent/received),
 *       plus 1% escrow on limit orders (refunded on cancel). Instant buy itself has
 *       no extra premium — you pay the listed sell offer price.</li>
 *   <li>Creation fee: ~20-1000 coins depending on duration (was 400). Capped around
 *       50k in legacy code but actually lower; we keep cap for safety.</li>
 * </ul>
 *
 * <p>Previous code used 5% instant-buy which made all bazaar flips look unprofitable.
 * Fixed to 1.25% per wiki + DevData verification.</p>
 */
public enum TaxTier {
    GENERAL(0.01d),
    ACCESSORY(0.01d), // was 3% historically; now 1% per 2025 rework, keep separate for tracking
    BAZAAR_INSTANT_BUY(0.0125d),
    BAZAAR_INSTANT_SELL(0.0125d),
    BAZAAR_CREATE(0.01d);

    public final double rate;
    TaxTier(double rate) { this.rate = rate; }
}
