package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class InventoryCloseEvent extends ZenithEvent {
    private final String title;

    public InventoryCloseEvent(String title) { this.title = title; }
    public String getTitle() { return title; }

    @Override public boolean isCancellable() { return false; }
}
