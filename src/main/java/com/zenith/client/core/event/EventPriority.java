package com.zenith.client.core.event;

/**
 * Priority levels for {@link ZenithEventBus} subscribers.
 *
 * <p>Lower numeric value = runs earlier. {@link #HIGHEST} subscribers fire first
 * and may cancel the event before downstream subscribers see it.</p>
 */
public enum EventPriority {

    HIGHEST(-100),
    HIGH(-50),
    NORMAL(0),
    LOW(50),
    LOWEST(100);

    private final int value;

    EventPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
