package com.zenith.client.engine.path.speed;

/**
 * Adaptive timer that delays per-tick movement to match the desired speed.
 * Used when we need to throttle movement (e.g. precise crop approaches) so
 * macros don't overshoot.
 */
public final class SpeedAdaptiveTimer {

    private double lastPosition;
    private long lastTickAt;
    private double measuredBps;

    public void reset(double position, long nowMs) { this.lastPosition = position; this.lastTickAt = nowMs; measuredBps = 0; }

    /** Call each tick with current along-axis position; returns measured blocks/s. */
    public double update(double position, long nowMs) {
        long dt = Math.max(1, nowMs - lastTickAt);
        double moved = Math.abs(position - lastPosition);
        double instBps = moved / (dt / 1000.0);
        measuredBps = measuredBps * 0.7 + instBps * 0.3;
        lastPosition = position;
        lastTickAt = nowMs;
        return measuredBps;
    }

    public double measured() { return measuredBps; }
}
