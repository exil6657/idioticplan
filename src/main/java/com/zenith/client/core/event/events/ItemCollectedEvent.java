package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when a notable item (rare drop, collection, tracked farming drop) is collected. */
public class ItemCollectedEvent extends ZenithEvent {
    private final String itemId;
    private final int count;
    private final boolean rare;
    public ItemCollectedEvent(String itemId, int count, boolean rare) { this.itemId = itemId; this.count = count; this.rare = rare; }
    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public boolean isRare() { return rare; }
    @Override public boolean isCancellable() { return false; }
}
