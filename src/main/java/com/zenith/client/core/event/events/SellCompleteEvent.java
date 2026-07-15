package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class SellCompleteEvent extends ZenithEvent {
    private final String itemId;
    private final int count;
    private final long coinsReceived;
    public SellCompleteEvent(String itemId, int count, long coinsReceived) {
        this.itemId = itemId; this.count = count; this.coinsReceived = coinsReceived;
    }
    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public long getCoinsReceived() { return coinsReceived; }
    @Override public boolean isCancellable() { return false; }
}
