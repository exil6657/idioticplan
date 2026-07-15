package com.zenith.client.engine.eyes.humanizer;

import com.zenith.client.core.timer.Timer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Introduces brief random pauses ("hesitations") mid-rotation, simulating the
 * human eyes/mind taking a brief detour or correcting course mid-sweep.
 *
 * <p>Each hesitation is a 40–120 ms freeze where angular velocity drops to 0
 * before resuming. Probability is checked every 100 ms of rotation.</p>
 */
public final class HesitationEngine {

    private boolean paused;
    private long resumeAt;
    private float chancePerCheck = 0.08f;
    private float maxPauseMs = 120f;
    private final Timer checkTimer = new Timer();

    public void configure(float chance, float maxPauseMs) {
        this.chancePerCheck = chance;
        this.maxPauseMs = maxPauseMs;
    }

    /** @return true if the rotation output should be held this frame. */
    public boolean isPaused(long nowMs) {
        if (paused) {
            if (nowMs >= resumeAt) paused = false;
            return true;
        }
        if (checkTimer.hasElapsed(100)) {
            checkTimer.reset();
            if (ThreadLocalRandom.current().nextFloat() < chancePerCheck) {
                paused = true;
                resumeAt = nowMs + 40 + (long) (ThreadLocalRandom.current().nextFloat() * (maxPauseMs - 40f));
                return true;
            }
        }
        return false;
    }

    public void reset() { paused = false; checkTimer.reset(); }
}
