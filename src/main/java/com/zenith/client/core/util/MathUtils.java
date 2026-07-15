package com.zenith.client.core.util;

import java.util.concurrent.ThreadLocalRandom;

/** Math helpers used across macros, rotation engines, and pathfinding. */
public final class MathUtils {

    private MathUtils() {}

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static float clampf(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int clampi(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Linear interpolation from a to b, with t clamped to [0,1]. */
    public static double lerpClamped(double a, double b, double t) {
        return lerp(a, b, clamp(t, 0d, 1d));
    }

    /** Shortest angle delta between yaw/pitch values in degrees, in [-180,180]. */
    public static float angleDelta(float a, float b) {
        float d = (a - b) % 360f;
        if (d > 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }

    public static double wrapDegrees(double v) {
        v = v % 360d;
        if (v >= 180d) v -= 360d;
        if (v < -180d) v += 360d;
        return v;
    }

    /** Distance in 2D (XZ plane only). */
    public static double distanceXZ(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2, dz = z1 - z2;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Random double in [min, max). */
    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static int randomInt(int min, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(min, maxInclusive + 1);
    }

    /** Gaussian random clamped to +/- 3-sigma and rescaled to range. */
    public static double randomGaussian(double mean, double stddev) {
        return ThreadLocalRandom.current().nextGaussian() * stddev + mean;
    }

    public static double roundTo(double v, int decimals) {
        double f = Math.pow(10, decimals);
        return Math.round(v * f) / f;
    }
}
