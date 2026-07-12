package com.zenith.client.engine.path.postprocess;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Adds a tiny overshoot/arrival "settle" at the last waypoint: the player
 * approaches slightly past the stop point then steps back 50-150 ms later,
 * mimicking a human not stopping perfectly on a dime.
 */
public final class ArrivalOvershoot {

    public boolean rollOvershoot() {
        return ThreadLocalRandom.current().nextFloat() < 0.4f;
    }

    /** @return overshoot offset blocks to add in facing direction (negative = overshoot past target). */
    public double overshootAmount() {
        return 0.05 + ThreadLocalRandom.current().nextDouble(0.08);
    }

    /** @return settle delay in ms between overshooting and correcting back. */
    public long settleDelayMs() {
        return 90 + ThreadLocalRandom.current().nextLong(160);
    }
}
