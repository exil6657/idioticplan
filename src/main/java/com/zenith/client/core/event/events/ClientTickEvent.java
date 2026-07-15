package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired at the start of every client tick (before world/entity tick). NOT cancellable. */
public class ClientTickEvent extends ZenithEvent {
    public final long tickCount;
    public ClientTickEvent(long tickCount) { this.tickCount = tickCount; }
    @Override public boolean isCancellable() { return false; }
}
