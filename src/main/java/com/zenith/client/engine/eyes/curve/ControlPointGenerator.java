package com.zenith.client.engine.eyes.curve;

import com.zenith.client.engine.eyes.RotationProfile;
import com.zenith.client.core.util.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates cubic-bezier control points for a rotation based on profile and
 * distance. Produces (cp1, cp2) in normalised [0..1] x [0..1] space.
 *
 * <p>Variation is introduced per-rotation (no two turns use identical control
 * points), biased by profile. This avoids the signature "perfect bezier" that
 * anticheats flag.</p>
 */
public final class ControlPointGenerator {

    private ControlPointGenerator() {}

    public static BezierRotationCurve.CP generate(RotationProfile profile, float angleDeg, boolean horizontal) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        // Base control points from profile.
        float cp1x = profile.cp1x;
        float cp1y = profile.cp1y;
        float cp2x = profile.cp2x;
        float cp2y = profile.cp2y;

        // Longer turns = more S-shape (cp1y lower, cp2y higher)
        float scale = MathUtils.clamp(Math.abs(angleDeg) / 180f, 0f, 1f);
        cp1y -= scale * 0.05f;
        cp2y += scale * 0.03f;

        // Horizontal (yaw) rotations get more natural bias than pitch; pitch (vertical) tends to be more linear.
        if (!horizontal) {
            cp1y = MathUtils.lerp(cp1y, 0.25f, 0.3f);
            cp2y = MathUtils.lerp(cp2y, 0.85f, 0.3f);
        }

        // Per-turn jitter.
        float jx = 0.03f + 0.02f * r.nextFloat();
        float jy = 0.03f + 0.02f * r.nextFloat();
        cp1x = MathUtils.clamp(cp1x + r.nextGaussian() * jx, 0.05, 0.5);
        cp1y = MathUtils.clamp(cp1y + r.nextGaussian() * jy, 0.0,  0.6);
        cp2x = MathUtils.clamp(cp2x + r.nextGaussian() * jx, 0.5,  0.95);
        cp2y = MathUtils.clamp(cp2y + r.nextGaussian() * jy, 0.4,  1.05);

        return new BezierRotationCurve.CP(cp1x, cp1y, cp2x, cp2y);
    }
}
