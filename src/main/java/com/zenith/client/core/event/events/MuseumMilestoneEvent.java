package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MuseumMilestoneEvent extends ZenithEvent {
    private final String milestone;
    public MuseumMilestoneEvent(String milestone) { this.milestone = milestone; }
    public String getMilestone() { return milestone; }
    @Override public boolean isCancellable() { return false; }
}
