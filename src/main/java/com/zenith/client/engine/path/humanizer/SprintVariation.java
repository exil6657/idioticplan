package com.zenith.client.engine.path.humanizer;

import com.zenith.client.core.timer.RandomDelay;
import com.zenith.client.core.timer.Timer;

import java.util.concurrent.ThreadLocalRandom;

/** Adds small burst/pause variation to sprinting so the player doesn't sprint at full tilt uniformly. */
public final class SprintVariation {

    private final Timer holdTimer = new Timer();
    private boolean sprinting;
    private long sprintDuration;

    public boolean update(boolean desiredSprint, double segmentRemaining) {
        if (holdTimer.hasElapsed(sprintDuration)) {
            holdTimer.reset();
            if (desiredSprint && ThreadLocalRandom.current().nextFloat() < 0.7f && segmentRemaining > 2) {
                sprinting = true;
                sprintDuration = RandomDelay.gaussian(600, 200);
            } else {
                sprinting = false;
                sprintDuration = 200 + ThreadLocalRandom.current().nextLong(400);
            }
        }
        return sprinting && desiredSprint;
    }
    public void reset() { sprinting = false; holdTimer.reset(); }
}
