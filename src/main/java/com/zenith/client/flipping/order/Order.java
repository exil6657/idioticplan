package com.zenith.client.flipping.order;

import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;

/**
 * Active order through the flipper pipeline. Wraps a FlipCandidate with state,
 * timestamps, retry count, and notes.
 */
public final class Order {
    public final FlipCandidate candidate;
    public volatile OrderState state = OrderState.PROPOSED;
    public long enteredStateAtMs;
    public int attempts = 0;
    public String note = "";
    public long buyPrice;
    public long listPrice;
    public long boughtAtMs;
    public long listedAtMs;
    public long soldAtMs;

    public Order(FlipCandidate c) {
        this.candidate = c;
        this.buyPrice = c.buyPrice;
        this.listPrice = c.sellPrice;
        this.enteredStateAtMs = System.currentTimeMillis();
    }

    public long heldMs() {
        long end = soldAtMs != 0 ? soldAtMs : System.currentTimeMillis();
        return end - (boughtAtMs != 0 ? boughtAtMs : enteredStateAtMs);
    }

    public void transition(OrderState s) {
        this.state = s;
        this.enteredStateAtMs = System.currentTimeMillis();
        if (s == OrderState.HOLDING) boughtAtMs = enteredStateAtMs;
        if (s == OrderState.LISTED) listedAtMs = enteredStateAtMs;
        if (s == OrderState.COMPLETED) soldAtMs = enteredStateAtMs;
    }

    public FlipType type() { return candidate.type; }
    public String itemId() { return candidate.itemId; }
}
