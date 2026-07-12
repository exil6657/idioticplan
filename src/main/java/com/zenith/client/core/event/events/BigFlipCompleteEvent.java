package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when a single flip exceeds the user's "big flip" threshold. */
public class BigFlipCompleteEvent extends ZenithEvent {
    private final String itemId;
    private final long profitCoins;
    public BigFlipCompleteEvent(String itemId, long profitCoins) { this.itemId = itemId; this.profitCoins = profitCoins; }
    public String getItemId() { return itemId; }
    public long getProfitCoins() { return profitCoins; }
    @Override public boolean isCancellable() { return false; }
}
