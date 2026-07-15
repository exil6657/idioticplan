package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class FlipCompleteEvent extends ZenithEvent {
    private final String itemId;
    private final long profitCoins;
    private final long flipDurationMs;
    public FlipCompleteEvent(String itemId, long profitCoins, long flipDurationMs) {
        this.itemId = itemId; this.profitCoins = profitCoins; this.flipDurationMs = flipDurationMs;
    }
    public String getItemId() { return itemId; }
    public long getProfitCoins() { return profitCoins; }
    public long getFlipDurationMs() { return flipDurationMs; }
    @Override public boolean isCancellable() { return false; }
}
