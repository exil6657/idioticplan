package com.zenith.client.engine.path.humanizer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Occasionally the player stutter-steps: moves forward briefly, releases, moves
 * again, mimicking adjusting position. Very low probability (~2% per second).
 */
public final class StutterStep {

    private long stutterEndAt;
    private boolean stuttering;
    private long nextStutterAt;

    public StutterStep() { nextStutterAt = System.currentTimeMillis() + 40_000; }

    public float applyForward(float desiredForward, long nowMs) {
        if (stuttering) {
            if (nowMs >= stutterEndAt) { stuttering = false; nextStutterAt = nowMs + 30_000 + ThreadLocalRandom.current().nextLong(60_000); }
            return desiredForward * 0.3f;
        }
        if (nowMs >= nextStutterAt) {
            stuttering = true;
            stutterEndAt = nowMs + 120 + ThreadLocalRandom.current().nextLong(200);
        }
        return desiredForward;
    }

    public void reset() { stuttering = false; nextStutterAt = System.currentTimeMillis() + 40_000; }
}
