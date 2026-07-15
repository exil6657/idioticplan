package com.zenith.client.engine.eyes.humanizer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Injects sub-pixel micro-jitter into rotations to simulate hand tremor and
 * mouse sensor noise. Jitter is low-amplitude (sigma ~0.08°) and high-frequency,
 * drawn from a Gaussian.
 */
public final class JitterInjector {

    private float sigma = 0.08f;

    public void setSigma(float sigma) { this.sigma = Math.max(0f, sigma); }

    /** @return yaw/pitch jitter offset to apply this frame. */
    public float[] sample() {
        if (sigma <= 0f) return new float[]{0f, 0f};
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return new float[]{
                (float) (r.nextGaussian() * sigma),
                (float) (r.nextGaussian() * sigma * 0.6f)
        };
    }
}
