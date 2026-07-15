package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class PINAttemptEvent extends ZenithEvent {
    private final boolean success;
    public PINAttemptEvent(boolean success) { this.success = success; }
    public boolean isSuccess() { return success; }
    @Override public boolean isCancellable() { return false; }
}
