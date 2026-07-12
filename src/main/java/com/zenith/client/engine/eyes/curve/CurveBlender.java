package com.zenith.client.engine.eyes.curve;

/**
 * Blends a bezier sample with a smoothstep weight; used by RotationExecutor to
 * combine the coarse bezier with gentle cubic smoothing.
 */
public final class CurveBlender {

    private CurveBlender() {}

    /**
     * @param bezierValue   value from bezier curve in [0,1]
     * @param cubicValue    value from smootherstep in [0,1]
     * @param cubicWeight   blend factor (0 = pure bezier, 1 = pure smoothstep)
     */
    public static float blend(float bezierValue, float cubicValue, float cubicWeight) {
        float w = Math.max(0f, Math.min(1f, cubicWeight));
        return bezierValue * (1f - w) + cubicValue * w;
    }
}
