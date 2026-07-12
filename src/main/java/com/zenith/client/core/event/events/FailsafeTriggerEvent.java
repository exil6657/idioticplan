package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class FailsafeTriggerEvent extends ZenithEvent {
    private final String failsafeName;
    private final String reason;
    private final int severity; // 0 = pause, 1 = warp, 2 = disconnect
    public FailsafeTriggerEvent(String failsafeName, String reason, int severity) {
        this.failsafeName = failsafeName; this.reason = reason; this.severity = severity;
    }
    public String getFailsafeName() { return failsafeName; }
    public String getReason() { return reason; }
    public int getSeverity() { return severity; }
    @Override public boolean isCancellable() { return true; } // allow cancelling low-severity triggers
}
