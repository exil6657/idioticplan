package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Raw mouse delta event (before sensitivity). Modify dx/dy to inject synthetic movement. */
public class MouseDeltaEvent extends ZenithEvent {
    public double dx, dy;
    public MouseDeltaEvent(double dx, double dy) { this.dx = dx; this.dy = dy; }
}
