package com.zenith.client.flipping;

/**
 * Classification of flip. Strategies filter by type.
 */
public enum FlipType {
    /** Buy BIN → relist BIN (classic AH flip). */
    AH_BIN,
    /** Bid on an auction → resell (chest/ending soon). */
    AH_BID,
    /** Bazaar instant-buy → instant-sell on a spread. */
    BAZAAR_SPREAD,
    /** Bazaar buy → craft → sell BIN. */
    CRAFT,
    /** NPC buy → AH/Bazaar sell. */
    NPC_RESELL,
    /** Buy enchanted book → apply to gear → resell. */
    BOOK_APPLY,
    /** Museum-donatable item price arbitrage (donation caps). */
    MUSEUM
}
