package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class JacobContestStartEvent extends ZenithEvent {
    private final String cropId;
    private final long timeRemainingMs;
    public JacobContestStartEvent(String cropId, long timeRemainingMs) { this.cropId = cropId; this.timeRemainingMs = timeRemainingMs; }
    public String getCropId() { return cropId; }
    public long getTimeRemainingMs() { return timeRemainingMs; }
    @Override public boolean isCancellable() { return false; }
}
