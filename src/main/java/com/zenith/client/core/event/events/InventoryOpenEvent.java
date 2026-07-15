package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class InventoryOpenEvent extends ZenithEvent {
    private final String title;
    private final int size;

    public InventoryOpenEvent(String title, int size) { this.title = title; this.size = size; }

    public String getTitle() { return title; }
    public int getSize() { return size; }

    @Override public boolean isCancellable() { return false; }
}
