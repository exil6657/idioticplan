package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired the moment the player enters the death screen. */
public class DeathEvent extends ZenithEvent {

    private final String cause;

    public DeathEvent(String cause) { this.cause = cause; }

    public String getCause() { return cause; }

    @Override public boolean isCancellable() { return false; }
}
