package com.zenith.client.engine.path.learning.profile;

import com.zenith.client.engine.path.learning.MovementProfile;

/**
 * Blends multiple profiles together (default + learned + friend-shared) into a
 * single effective profile with weights. Used when loading a profile that has
 * few samples — we interpolate toward the default to keep behaviour believable.
 */
public final class ProfileBlender {

    public static MovementProfile blend(MovementProfile a, MovementProfile b, double w) {
        MovementProfile out = DefaultProfile.build();
        out.blend(a, 1.0);
        out.blend(b, w);
        return out;
    }

    private ProfileBlender() {}
}
