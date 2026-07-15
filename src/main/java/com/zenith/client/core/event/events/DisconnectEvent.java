package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when the client disconnects from a server (voluntary or kicked). */
public class DisconnectEvent extends ZenithEvent {

    private final String reason;

    public DisconnectEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override public boolean isCancellable() { return false; }
}
