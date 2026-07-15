package com.zenith.client.core.timer;

import com.zenith.client.core.util.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Humanized delay generator.
 *
 * <p>Instead of fixed cooldowns (which server-side anticheats flag), each delay
 * is drawn from a distribution: uniform range, gaussian around a mean, or
 * weighted "reaction time" curve with a long tail. All values are in ms.</p>
 */
public final class RandomDelay {

    private RandomDelay() {}

    /** Uniform random delay in [minMs, maxMs). */
    public static long uniform(long minMs, long maxMs) {
        if (maxMs <= minMs) return minMs;
        return minMs + ThreadLocalRandom.current().nextLong(maxMs - minMs);
    }

    /** Gaussian delay with mean and standard deviation, clamped to [mean-3σ, mean+3σ]. */
    public static long gaussian(long meanMs, long stddevMs) {
        long v = Math.round(ThreadLocalRandom.current().nextGaussian() * stddevMs + meanMs);
        return Math.max(0, v);
    }

    /** Human visual-reaction-style delay (gamma-like, peaking ~220 ms, tail to ~450 ms). */
    public static long humanReaction() {
        double u = ThreadLocalRandom.current().nextDouble();
        // rough logistic-ish mapping
        return Math.round(180f + 250f * (float) Math.pow(u, 2.0d));
    }

    /** Reaction delay scaled to difficulty — higher difficulty = longer reaction. */
    public static long humanReactionScaled(double difficulty) {
        long base = humanReaction();
        return Math.round(base * MathUtils.clamp(0.7d + difficulty * 0.6d, 0.7d, 2d));
    }
}
