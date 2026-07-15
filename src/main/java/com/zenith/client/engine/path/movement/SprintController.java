package com.zenith.client.engine.path.movement;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Decides when to sprint based on distance, angle, and randomness, matching
 * human sprint-tap behaviour. Humans don't hold sprint 100% — they tap it at
 * the start of longer segments, release on turns, etc.
 */
public final class SprintController {

    private boolean sprinting;
    private long sprintStopAt;
    private double minDistanceForSprint = 3.5d;

    public void setMinDistanceForSprint(double v) { this.minDistanceForSprint = v; }

    public boolean shouldSprint(double segmentLength, float angleAhead, boolean jumpQueued, long nowMs) {
        if (sprinting && nowMs < sprintStopAt) return true;
        if (jumpQueued) {
            // Sprint-jump only for long jumps (>2 block gaps).
            sprinting = segmentLength > 2.5d;
        } else {
            if (Math.abs(angleAhead) > 35f) { sprinting = false; return false; }
            if (segmentLength > minDistanceForSprint && ThreadLocalRandom.current().nextFloat() < 0.75f) {
                sprinting = true;
                // Sprint for 400-1200 ms, then re-evaluate (humans release and re-tap).
                sprintStopAt = nowMs + 400 + ThreadLocalRandom.current().nextLong(800);
            } else {
                sprinting = false;
            }
        }
        return sprinting;
    }

    public void stop() { sprinting = false; }
    public boolean isSprinting() { return sprinting; }
}
