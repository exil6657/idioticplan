package com.zenith.client.flipping.tax;

/**
 * Cached tax badge for an item — looked up once per item and cached by the
 * profit calculator. ACCESSORY vs GENERAL is all we need for Phase 9 tax math.
 */
public enum TaxBadge {
    GENERAL(TaxTier.GENERAL, false),
    ACCESSORY(TaxTier.ACCESSORY, true);

    public final TaxTier tier;
    public final boolean accessory;
    TaxBadge(TaxTier tier, boolean accessory) { this.tier = tier; this.accessory = accessory; }
}
