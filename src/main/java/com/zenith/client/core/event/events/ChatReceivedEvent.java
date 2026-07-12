package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/**
 * Fired when a chat message is received from the server (pre-GUI render).
 * Cancel to suppress the message from appearing in the HUD.
 */
public class ChatReceivedEvent extends ZenithEvent {

    private String message;
    /** Original MC-formatted string (§ codes). */
    private final String rawFormatted;
    /** Chat type id (0=chat, 1=system, 2=action bar). */
    private final int typeId;

    public ChatReceivedEvent(String message, String rawFormatted, int typeId) {
        this.message = message;
        this.rawFormatted = rawFormatted;
        this.typeId = typeId;
    }

    public String getMessage() { return message; }
    public String getRawFormatted() { return rawFormatted; }
    public int getTypeId() { return typeId; }
    public void setMessage(String message) { this.message = message; }
}
