package com.zenith.client.engine.eyes.humanizer;

/**
 * Quantises rotation output to a fixed rate (e.g. 20 Hz for in-tick updates,
 * 120 Hz for full mouse-poll rate). Between ticks, outputs are held so the
 * movement looks discrete rather than continuous (mice are sampled at finite
 * rates; perfect per-frame angles look robotic).
 */
public final class TickQuantizer {

    private float hz = 120f;
    private long lastSampleNs;
    private float lastYaw, lastPitch;
    private boolean primed;

    public void setHz(float hz) { this.hz = Math.max(20f, Math.min(1000f, hz)); }

    public float getHz() { return hz; }

    /**
     * @param desiredYaw   ideal yaw delta for this frame
     * @param desiredPitch ideal pitch delta for this frame
     * @param nowNs        current time in nanoseconds
     * @return quantised deltas to apply (may be 0 if we haven't reached next sample)
     */
    public float[] quantize(float desiredYaw, float desiredPitch, long nowNs) {
        if (!primed) {
            primed = true;
            lastSampleNs = nowNs;
            lastYaw = desiredYaw;
            lastPitch = desiredPitch;
            return new float[]{desiredYaw, desiredPitch};
        }
        long intervalNs = (long) (1_000_000_000d / hz);
        if (nowNs - lastSampleNs < intervalNs) {
            return new float[]{0f, 0f};
        }
        lastSampleNs = nowNs;
        lastYaw = desiredYaw;
        lastPitch = desiredPitch;
        return new float[]{lastYaw, lastPitch};
    }

    public void reset() { primed = false; lastYaw = 0f; lastPitch = 0f; }
}
