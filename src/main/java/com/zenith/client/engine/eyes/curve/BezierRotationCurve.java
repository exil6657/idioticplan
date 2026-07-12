package com.zenith.client.engine.eyes.curve;

/**
 * Cubic bezier in 2D (time→angle-fraction). Given (x1,y1) (x2,y2) control points,
 * it maps an input progress t in [0,1] to a y-value that represents how far
 * along the rotation should be. The curve is sampled with a Newton-Raphson
 * inversion so we can evaluate y_at(t) efficiently.
 */
public class BezierRotationCurve {

    public record CP(float x1, float y1, float x2, float y2) {}

    private final float x1, y1, x2, y2;

    public BezierRotationCurve(CP cp) {
        this(cp.x1(), cp.y1(), cp.x2(), cp.y2());
    }

    public BezierRotationCurve(float x1, float y1, float x2, float y2) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }

    /** @return fraction of rotation achieved given elapsed fraction t in [0,1]. */
    public float sample(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        // Solve for parametric u s.t. bezierX(u) = t, then return bezierY(u).
        float u = solveCurveX(t);
        return bezierY(u);
    }

    private float bezierX(float u) {
        float mu = 1f - u;
        return 3f * mu * mu * u * x1 + 3f * mu * u * u * x2 + u * u * u;
    }
    private float bezierY(float u) {
        float mu = 1f - u;
        return 3f * mu * mu * u * y1 + 3f * mu * u * u * y2 + u * u * u;
    }
    private float bezierDX(float u) {
        float mu = 1f - u;
        return 3f * mu * mu * x1 + 6f * mu * u * (x2 - x1) + 3f * u * u * (1f - x2);
    }

    private float solveCurveX(float x) {
        // Newton-Raphson with 8 iterations, fallback to bisection.
        float u = x;
        for (int i = 0; i < 8; i++) {
            float cx = bezierX(u) - x;
            if (Math.abs(cx) < 1e-3f) return u;
            float dx = bezierDX(u);
            if (Math.abs(dx) < 1e-6f) break;
            u -= cx / dx;
        }
        // Bisection fallback.
        float lo = 0f, hi = 1f;
        u = x;
        while (lo < hi) {
            float cx = bezierX(u);
            if (Math.abs(cx - x) < 1e-3f) return u;
            if (cx > x) hi = u; else lo = u;
            u = (lo + hi) * 0.5f;
        }
        return u;
    }
}
