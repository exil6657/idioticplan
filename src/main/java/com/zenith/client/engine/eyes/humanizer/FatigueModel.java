package com.zenith.client.engine.eyes.humanizer;

/**
 * Models player fatigue as a function of time-played, increasing pauses,
 * slowing rotations, and amplifying tremor slightly — not enough to be
 * obvious, just enough to prevent perfect consistency over many hours.
 */
public final class FatigueModel {

    private float fatigue = 0f;

    /** Update fatigue based on elapsed play minutes (0..240). */
    public void update(float minutesPlayed) {
        fatigue = Math.min(1f, minutesPlayed / 180f);
    }

    /** @return duration multiplier (1..max). */
    public float durationMultiplier(float maxFatigue) {
        return 1f + (maxFatigue - 1f) * fatigue;
    }

    /** @return added jitter sigma from fatigue (up to 0.05 extra deg). */
    public float extraJitter() { return 0.05f * fatigue; }

    /** @return additional hesitation chance from fatigue. */
    public float extraHesitation() { return 0.04f * fatigue; }

    public void reset() { fatigue = 0f; }
    public float getFatigue() { return fatigue; }
}
