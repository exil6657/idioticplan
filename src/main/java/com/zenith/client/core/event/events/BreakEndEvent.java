package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class BreakEndEvent extends ZenithEvent {
    private final long actualDurationMs;
    public BreakEndEvent(long actualDurationMs) { this.actualDurationMs = actualDurationMs; }
    public long getActualDurationMs() { return actualDurationMs; }
    @Override public boolean isCancellable() { return false; }
}
