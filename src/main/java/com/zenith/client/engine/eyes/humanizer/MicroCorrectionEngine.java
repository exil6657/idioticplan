package com.zenith.client.engine.eyes.humanizer;

import com.zenith.client.core.util.MathUtils;

/**
 * Applies tiny post-turn micro-corrections (1–3 small nudges) once the main
 * rotation has ended, simulating the human "zeroing in" on a precise point.
 */
public final class MicroCorrectionEngine {

    private boolean active;
    private int correctionsPlanned;
    private int correctionsDone;
    private long correctionStartMs;
    private long nextCorrectionAt;
    private float residualYaw, residualPitch;

    /** Begin the micro-correction phase given a remaining residual error. */
    public void start(float residualYaw, float residualPitch, long now) {
        this.residualYaw = residualYaw;
        this.residualPitch = residualPitch;
        float absResidual = Math.max(Math.abs(residualYaw), Math.abs(residualPitch));
        if (absResidual < 0.15f) { active = false; return; }
        correctionsPlanned = (int) MathUtils.clamp(Math.ceil(absResidual / 0.6f), 1, 3);
        correctionsDone = 0;
        correctionStartMs = now;
        nextCorrectionAt = now + 90 + (long) (Math.random() * 120);
        active = true;
    }

    /** Called each tick; returns yaw/pitch delta to apply this frame. */
    public float[] tick(long now, float dtMs) {
        if (!active) return new float[]{0f, 0f};
        if (now < nextCorrectionAt) return new float[]{0f, 0f};
        // Apply one correction: step 60% of remaining residual, then schedule next.
        float stepYaw = residualYaw * 0.6f;
        float stepPitch = residualPitch * 0.6f;
        residualYaw -= stepYaw;
        residualPitch -= stepPitch;
        correctionsDone++;
        if (correctionsDone >= correctionsPlanned ||
            (Math.abs(residualYaw) < 0.05f && Math.abs(residualPitch) < 0.05f)) {
            active = false;
        } else {
            nextCorrectionAt = now + 80 + (long) (Math.random() * 140);
        }
        return new float[]{ stepYaw, stepPitch };
    }

    public boolean isActive() { return active; }
    public void reset() { active = false; }
}
