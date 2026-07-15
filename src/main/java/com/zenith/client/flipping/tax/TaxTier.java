package com.zenith.client.flipping.tax;

/**
 * AH tax brackets on Hypixel SkyBlock. As of the most recent rework:
 *
 * <ul>
 *   <li>Base AH tax: 1% on all sales.</li>
 *   <li>Accessories: 3%.</li>
 *   <li>Co-op penalty: +1% when sold in co-op (ignored in Phase 9).</li>
 * </ul>
 *
 * <p>Additionally there's a 400k creation fee per auction list. Bazaar fees are
 * 5% instant-buy and 1.25% instant-sell.</p>
 */
public enum TaxTier {
    GENERAL(0.01d),
    ACCESSORY(0.03d),
    BAZAAR_INSTANT_BUY(0.05d),
    BAZAAR_INSTANT_SELL(0.0125d);

    public final double rate;
    TaxTier(double rate) { this.rate = rate; }
}
