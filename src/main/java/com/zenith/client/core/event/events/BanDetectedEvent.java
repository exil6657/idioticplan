package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when chat patterns indicate the player has been banned/muted. */
public class BanDetectedEvent extends ZenithEvent {

    private final String reason;
    private final long durationMinutes; // -1 = permanent
    private final boolean mute;

    public BanDetectedEvent(String reason, long durationMinutes, boolean mute) {
        this.reason = reason;
        this.durationMinutes = durationMinutes;
        this.mute = mute;
    }

    public String getReason() { return reason; }
    public long getDurationMinutes() { return durationMinutes; }
    public boolean isMute() { return mute; }

    @Override public boolean isCancellable() { return false; }
}
