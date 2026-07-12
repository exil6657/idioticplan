package com.zenith.client.engine.eyes.humanizer;

/**
 * Converts desired world-space angular velocity to "mouse delta" units,
 * modelling the in-game sensitivity slider and mouse DPI.
 *
 * <p>The conversion is:
 *   delta_yaw_per_mouse_unit = sensitivity * 0.15f * (1 + mcaModifier)
 * where mcaModifier is the MouseSensitivity option from MC Options (default 0.5
 * → 100% in options GUI). This lets us work in delta-units internally (the
 * actual axis we move the mouse on) instead of raw yaw degrees, which makes
 * acceleration & jitter feel natural.</p>
 */
public final class MouseSensitivitySimulator {

    /** MC Options mouse sensitivity (0..1 as exposed by Options.sensitivity). Default 0.5. */
    private float sensitivity = 0.5f;
    /** Effective conversion factor, recomputed when sensitivity changes. */
    private float factor = computeFactor(0.5f);

    public void setSensitivity(float mcSensitivity) {
        this.sensitivity = Math.max(0f, Math.min(1f, mcSensitivity));
        this.factor = computeFactor(this.sensitivity);
    }

    public float getSensitivity() { return sensitivity; }

    /** Convert a desired yaw/pitch delta (degrees) to a mouse-unit delta. */
    public float degreesToMouseDelta(float degrees) {
        return degrees / factor;
    }

    /** Convert a mouse-unit delta back to degrees. */
    public float mouseDeltaToDegrees(float mouseDelta) {
        return mouseDelta * factor;
    }

    private static float computeFactor(float s) {
        // Empirically matches MC 26.1's sensitivity curve (linear until ~100%, then nonlinear).
        float f = s * 0.6f + 0.2f;
        return f * f * f * 8f * 0.15f;
    }
}
