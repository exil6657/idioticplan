package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when the client opens or closes a Screen. */
public class ScreenChangedEvent extends ZenithEvent {

    private final String newTitle;
    private final boolean opened;

    public ScreenChangedEvent(String newTitle, boolean opened) {
        this.newTitle = newTitle;
        this.opened = opened;
    }

    public String getNewTitle() { return newTitle; }
    public boolean isOpened() { return opened; }
    public boolean isClosed() { return !opened; }

    @Override public boolean isCancellable() { return false; }
}
