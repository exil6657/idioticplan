package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when a scheduled /user-initiated macro break begins. */
public class BreakStartEvent extends ZenithEvent {
    private final long scheduledDurationMs;
    public BreakStartEvent(long scheduledDurationMs) { this.scheduledDurationMs = scheduledDurationMs; }
    public long getScheduledDurationMs() { return scheduledDurationMs; }
    @Override public boolean isCancellable() { return false; }
}
