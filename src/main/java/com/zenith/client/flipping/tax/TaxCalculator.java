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
     * Net coins spent when instant-buying a bazaar offer (you pay a ~5% premium over sell price).
     */
    public static long bazaarInstantBuyCost(long unitPrice, int count) {
        double rate = TaxTier.BAZAAR_INSTANT_BUY.rate;
        return (long) Math.ceil(unitPrice * (1d + rate)) * (long) count;
    }

    /**
     * Net coins received when instant-selling to a buy order.
     */
    public static long bazaarInstantSellReceived(long unitPrice, int count) {
        double rate = TaxTier.BAZAAR_INSTANT_SELL.rate;
        return (long) Math.floor(unitPrice * (1d - rate)) * (long) count;
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
