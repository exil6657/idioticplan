package com.zenith.client.engine.path.humanizer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds natural timing to jumps: humans don't jump on the exact tick they reach
 * a block edge — they jump 50–150 ms before, with a slight left/right strafe
 * bias in the air.
 */
public final class JumpVariation {

    private long nextJumpOffsetMs;
    private boolean primed;

    /** Call when a jump is queued; returns ms-before-edge to fire the jump input. */
    public long primeJump() {
        if (primed) return nextJumpOffsetMs;
        primed = true;
        nextJumpOffsetMs = -ThreadLocalRandom.current().nextLong(40, 130);
        return nextJumpOffsetMs;
    }

    /** @return a small strafe bias (-0.15..0.15) to apply mid-air for non-robotic jumps. */
    public float airStrafeBias() {
        return (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.3f;
    }

    public void reset() { primed = false; nextJumpOffsetMs = 0; }
}
