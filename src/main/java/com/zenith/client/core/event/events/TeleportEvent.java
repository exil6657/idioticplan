package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when the player undergoes a server-initiated teleport. */
public class TeleportEvent extends ZenithEvent {

    private final double fromX, fromY, fromZ;
    private final double toX, toY, toZ;

    public TeleportEvent(double fromX, double fromY, double fromZ,
                         double toX, double toY, double toZ) {
        this.fromX = fromX; this.fromY = fromY; this.fromZ = fromZ;
        this.toX = toX;     this.toY = toY;     this.toZ = toZ;
    }

    public double getFromX() { return fromX; }
    public double getFromY() { return fromY; }
    public double getFromZ() { return fromZ; }
    public double getToX() { return toX; }
    public double getToY() { return toY; }
    public double getToZ() { return toZ; }

    public double distance() {
        double dx = toX - fromX, dy = toY - fromY, dz = toZ - fromZ;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    @Override public boolean isCancellable() { return false; }
}
