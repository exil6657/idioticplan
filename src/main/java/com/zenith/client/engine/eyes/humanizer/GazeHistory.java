package com.zenith.client.engine.eyes.humanizer;

import java.util.Arrays;

/**
 * Rolling window of recent gaze velocities (yaw/pitch deltas per ms) used by
 * other humanizers to maintain temporal coherence — we don't want sudden
 * discontinuities in angular speed, real humans don't do that.
 */
public final class GazeHistory {

    private final int windowSize;
    private final float[] yawVel;
    private final float[] pitchVel;
    private int idx = 0;
    private int count = 0;

    public GazeHistory(int windowSize) {
        this.windowSize = windowSize;
        this.yawVel = new float[windowSize];
        this.pitchVel = new float[windowSize];
    }

    public void push(float yawDeltaPerMs, float pitchDeltaPerMs) {
        yawVel[idx] = yawDeltaPerMs;
        pitchVel[idx] = pitchDeltaPerMs;
        idx = (idx + 1) % windowSize;
        if (count < windowSize) count++;
    }

    public float meanYawVel() { return (float) Arrays.stream(yawVel).limit(count).average().orElse(0f); }
    public float meanPitchVel() { return (float) Arrays.stream(pitchVel).limit(count).average().orElse(0f); }

    /** Peak absolute velocity over the window. */
    public float peakYawVel() {
        float m = 0f; for (int i = 0; i < count; i++) m = Math.max(m, Math.abs(yawVel[i])); return m;
    }

    public void reset() { idx = 0; count = 0; Arrays.fill(yawVel, 0f); Arrays.fill(pitchVel, 0f); }
}
