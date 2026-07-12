package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired on coop chat messages (so auto-pause can be triggered). */
public class CoopMessageEvent extends ZenithEvent {

    private final String sender;
    private final String message;

    public CoopMessageEvent(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
}
