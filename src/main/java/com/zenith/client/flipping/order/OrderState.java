package com.zenith.client.flipping.order;

/** Lifecycle states for a flip order. */
public enum OrderState {
    /** Candidate has been proposed but not yet validated against budget/filter. */
    PROPOSED,
    /** Queued to buy; awaiting next AH navigation tick. */
    QUEUED_TO_BUY,
    /** Navigating to the auction house / finding the listing. */
    NAVIGATING,
    /** Click the BIN / confirm button. */
    BUYING,
    /** Item acquired; queued to relist. */
    HOLDING,
    /** Listing created; waiting for sale. */
    LISTED,
    /** Sale detected; collecting coins. */
    COLLECTING,
    /** Successfully sold — recorded in profit tracker. */
    COMPLETED,
    /** Cancelled/failed (failsafe, AH down, listing gone). */
    FAILED
}
