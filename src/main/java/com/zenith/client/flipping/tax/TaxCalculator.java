package com.zenith.client.flipping.tax;

/**
 * Computes the net proceeds from an AH/Bazaar listing after tax and listing
 * fees. All calculations use integer coin units (long) to avoid float error.
 *
 * <p>Listing fee is fixed per auction (currently 400 coins per item).
 */
public final class TaxCalculator {

    public static final long LISTING_FEE = 400L;
    public static final long BIN_CREATE_FEE_CAP = 50_000L;

    private TaxCalculator() {}

    /**
     * Net coins received after selling {@code count} items at {@code unitPrice} BIN.
     * Takes base tax + accessory premium if the item is an accessory tier.
     */
    public static long netFromBin(long unitPrice, int count, boolean accessory) {
        long gross = unitPrice * (long) count;
        double rate = accessory ? TaxTier.ACCESSORY.rate : TaxTier.GENERAL.rate;
        long fee = (long) Math.ceil(gross * rate);
        long list = Math.min(LISTING_FEE * (long) count, BIN_CREATE_FEE_CAP);
        return gross - fee - list;
    }

    /** Shortcut for non-accessory single-item sales. */
    public static long netFromBin(long unitPrice) { return netFromBin(unitPrice, 1, false); }

    public static long netFromBid(long finalBid) {
        long fee = (long) Math.ceil(finalBid * TaxTier.GENERAL.rate);
        return finalBid - fee;
    }

    /**
     * Net coins spent when instant-buying a bazaar offer.
     * Real Bazaar: you pay exactly the sell offer price (no premium); 1.25% is charged
     * on the seller's side, not buyer. For profit calc we include the fee on sell side only,
     * but we keep this method returning gross cost (historical code added 5% which killed profit).
     */
    public static long bazaarInstantBuyCost(long unitPrice, int count) {
        // Corrected: no extra fee on buy side for instant buy; seller pays tax.
        // If you want to model the 1.25% as buyer cost, use rate, but wiki says sell-side only.
        // We return gross cost so SpreadCalculator comparisons are accurate.
        return unitPrice * (long) count;
    }

    /**
     * Net coins received when instant-selling to a buy order (seller pays 1.25%).
     */
    public static long bazaarInstantSellReceived(long unitPrice, int count) {
        double rate = TaxTier.BAZAAR_INSTANT_SELL.rate;
        return (long) Math.floor(unitPrice * (1d - rate)) * (long) count;
    }

    /**
     * Net coins received after a limit sell order fills (seller pays 1% create + 1.25% sell = ~1.25% net).
     * For simplicity use SELL rate.
     */
    public static long bazaarLimitSellReceived(long unitPrice, int count) {
        double rate = TaxTier.BAZAAR_INSTANT_SELL.rate;
        return (long) Math.floor(unitPrice * (1d - rate)) * (long) count;
    }

    /**
     * Coins escrowed when creating a buy order (buy price + 1% tax held).
     */
    public static long bazaarCreateBuyCost(long unitPrice, int count) {
        double rate = TaxTier.BAZAAR_CREATE.rate;
        return (long) Math.ceil(unitPrice * (1d + rate)) * (long) count;
    }

    /**
     * The list price needed to net {@code targetCoins} after tax (for undercut calculation).
     * Uses the same rate as {@link #netFromBin(long)}.
     */
    public static long listPriceForNet(long netTarget, boolean accessory) {
        double rate = accessory ? TaxTier.ACCESSORY.rate : TaxTier.GENERAL.rate;
        // net = price - price*rate - listFee  =>  price = (net + listFee) / (1 - rate)
        double raw = (netTarget + LISTING_FEE) / (1d - rate);
        return Math.max(1L, (long) Math.ceil(raw));
    }
}
