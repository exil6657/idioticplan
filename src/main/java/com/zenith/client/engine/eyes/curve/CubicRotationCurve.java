package com.zenith.client.engine.eyes.curve;

/**
 * 1D cubic smoothstep (ease-in-out) used to blend on top of the bezier curve.
 *
 * <p>f(t) = t²(3 − 2t), which has zero first derivative at 0 and 1. Applied at
 * small weight to remove sharp entry/exit jerks.</p>
 */
public final class CubicRotationCurve {

    private CubicRotationCurve() {}

    public static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    /** Smootherstep (Ken Perlin): 6t⁵−15t⁴+10t³. */
    public static float smootherstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }
}
