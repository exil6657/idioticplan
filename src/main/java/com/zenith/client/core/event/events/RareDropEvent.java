package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class RareDropEvent extends ZenithEvent {
    private final String itemId;
    private final String itemName;
    private final long estimatedValueCoins;
    public RareDropEvent(String itemId, String itemName, long estimatedValueCoins) {
        this.itemId = itemId; this.itemName = itemName; this.estimatedValueCoins = estimatedValueCoins;
    }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public long getEstimatedValueCoins() { return estimatedValueCoins; }
    @Override public boolean isCancellable() { return false; }
}
