package com.zenith.client.engine.eyes.humanizer;

import com.zenith.client.core.util.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Probabilistically applies a small overshoot at the end of a rotation, then
 * lets the MicroCorrectionEngine pull back. This mimics the human tendency to
 * slightly overshoot a target when making a fast flick and then nudge back.
 */
public final class OvershootCorrector {

    private boolean active = false;
    private float overshootYaw, overshootPitch;
    private float remaining;
    private float phase;

    /**
     * Called once at rotation start to roll the dice on whether to overshoot.
     * @return true if an overshoot will occur
     */
    public boolean roll(float angleDeg, float chance, float maxDegrees) {
        active = ThreadLocalRandom.current().nextFloat() < chance && Math.abs(angleDeg) > 15f;
        if (!active) return false;
        float magnitude = ThreadLocalRandom.current().nextFloat() * Math.min(maxDegrees, Math.abs(angleDeg) * 0.12f + 1.0f);
        // 60% of overshoots are in the direction of rotation (past the target)
        float sign = ThreadLocalRandom.current().nextFloat() < 0.6f ? Math.signum(angleDeg) : -Math.signum(angleDeg);
        overshootYaw = magnitude * sign;
        overshootPitch = magnitude * sign * 0.5f * (ThreadLocalRandom.current().nextFloat() - 0.5f);
        remaining = 1f;
        phase = 0f;
        return true;
    }

    /**
     * Apply overshoot offset once the base curve passes t > 0.92 (approaching end).
     * Returns yaw/pitch offsets to ADD to the curve output.
     */
    public float[] apply(float t) {
        if (!active) return new float[]{0f, 0f};
        if (t < 0.88f) return new float[]{0f, 0f};
        // Ramp the overshoot in from 0.88 -> 1.0 and then back out as micro-correct takes over.
        float u = (t - 0.88f) / 0.12f;
        float shape = (float) Math.sin(MathUtils.clamp(u, 0f, 1f) * Math.PI);
        return new float[]{ overshootYaw * shape, overshootPitch * shape };
    }

    public boolean isActive() { return active; }
    public void reset() { active = false; overshootYaw = 0f; overshootPitch = 0f; }
}
