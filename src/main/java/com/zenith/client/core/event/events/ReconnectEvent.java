package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired after a successful auto-reconnect completes. */
public class ReconnectEvent extends ZenithEvent {

    private final String serverAddress;
    private final long disconnectMs;

    public ReconnectEvent(String serverAddress, long disconnectMs) {
        this.serverAddress = serverAddress;
        this.disconnectMs = disconnectMs;
    }

    public String getServerAddress() { return serverAddress; }
    public long getDisconnectMs() { return disconnectMs; }

    @Override public boolean isCancellable() { return false; }
}
