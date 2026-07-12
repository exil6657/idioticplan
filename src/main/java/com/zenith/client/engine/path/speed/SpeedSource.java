package com.zenith.client.engine.path.speed;

/** Speed source — yields a movement speed in blocks per second. */
public interface SpeedSource {
    /**
     * @return current speed contribution in blocks/s, or 0 if not currently active.
     */
    double currentSpeedBps();
    /** Priority (higher = applied first). */
    default int priority() { return 0; }
}
