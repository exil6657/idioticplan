package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class PestSpawnEvent extends ZenithEvent {
    private final String pestType;
    public PestSpawnEvent(String pestType) { this.pestType = pestType; }
    public String getPestType() { return pestType; }
    @Override public boolean isCancellable() { return false; }
}
