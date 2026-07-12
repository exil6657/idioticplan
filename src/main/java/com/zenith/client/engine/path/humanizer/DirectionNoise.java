package com.zenith.client.engine.path.humanizer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds subtle random strafe noise while moving forward — players don't walk
 * perfectly straight, they wobble a few degrees left/right with a slow sinusoid
 * plus fast micro-corrections.
 */
public final class DirectionNoise {

    private double phase;
    private double period;
    private double microPhase;
    private double microPeriod;

    public DirectionNoise() { reroll(); }

    private void reroll() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        phase = r.nextDouble(Math.PI*2);
        period = 1800 + r.nextDouble(2200);
        microPhase = r.nextDouble(Math.PI*2);
        microPeriod = 250 + r.nextDouble(400);
    }

    /** @return strafe value in [-0.08, 0.08] to add this tick. */
    public float sample(long nowMs) {
        double big = Math.sin(nowMs * Math.PI*2 / period + phase) * 0.05;
        double micro = Math.sin(nowMs * Math.PI*2 / microPeriod + microPhase) * 0.03;
        if (ThreadLocalRandom.current().nextFloat() < 0.002) reroll();
        return (float) (big + micro);
    }
}
