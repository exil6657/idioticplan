package com.zenith.client.engine.path.learning.profile;

import com.zenith.client.engine.path.learning.MovementProfile;

/**
 * Blends a newly-learned sample into an existing profile, weighted by sample
 * count so the profile converges over time without flipping.
 */
public final class ProfileMerger {

    /**
     * @param current   the running profile (mutated in place)
     * @param learned   the new profile built from a sample window
     * @param weight    0..1 blend weight for the new sample (lower = more conservative)
     */
    public static void merge(MovementProfile current, MovementProfile learned, double weight) {
        if (current == null || learned == null) return;
        current.blend(learned, weight);
        ProfileValidator.validate(current);
    }

    private ProfileMerger() {}
}
