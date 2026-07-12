package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when the failsafe detects the player has been moved to Hypixel limbo. */
public class LimboDetectedEvent extends ZenithEvent {

    private final String triggerReason;

    public LimboDetectedEvent(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public String getTriggerReason() { return triggerReason; }

    @Override public boolean isCancellable() { return false; }
}
