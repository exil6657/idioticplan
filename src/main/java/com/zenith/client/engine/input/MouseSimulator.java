package com.zenith.client.engine.input;

/**
 * Accumulates mouse-delta values (in Minecraft "delta units", not raw pixels)
 * to be applied once per tick/frame. ZenithEyes feeds yaw/pitch deltas into
 * this class; the MixinMouse hook (Phase 4) then forwards them to the mouse
 * handler before MC processes them.
 */
public final class MouseSimulator {

    private double deltaX, deltaY;
    private boolean dirty;
    private int clickLeft, clickRight;
    private int wheelDelta;

    public synchronized void move(double dx, double dy) {
        deltaX += dx; deltaY += dy; dirty = true;
    }

    public synchronized void leftClick(int clicks) { clickLeft += clicks; }
    public synchronized void rightClick(int clicks) { clickRight += clicks; }
    public synchronized void wheel(int dw) { wheelDelta += dw; }

    public synchronized double[] pollMove() {
        double[] out = {deltaX, deltaY};
        deltaX = 0; deltaY = 0; dirty = false;
        return out;
    }

    public synchronized int pollLeftClicks()  { int v = clickLeft; clickLeft = 0; return v; }
    public synchronized int pollRightClicks() { int v = clickRight; clickRight = 0; return v; }
    public synchronized int pollWheel() { int v = wheelDelta; wheelDelta = 0; return v; }

    public boolean isDirty() { return dirty; }
}
