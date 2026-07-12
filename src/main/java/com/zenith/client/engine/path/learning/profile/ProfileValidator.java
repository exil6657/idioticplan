package com.zenith.client.engine.path.learning.profile;

import com.zenith.client.engine.path.learning.MovementProfile;

/** Sanity-checks a profile (clamps out-of-range values to sensible bounds). */
public final class ProfileValidator {

    public static MovementProfile validate(MovementProfile p) {
        if (p == null) return DefaultProfile.build();
        p.turnMeanDegPerSec    = clamp(p.turnMeanDegPerSec, 40, 400);
        p.turnStdDegPerSec     = clamp(p.turnStdDegPerSec, 5, 120);
        p.overshootMeanDeg     = clamp(p.overshootMeanDeg, 0, 8);
        p.overshootChance      = clamp(p.overshootChance, 0, 1);
        p.walkSpeedMean        = clamp(p.walkSpeedMean, 3, 6);
        p.sprintSpeedMean      = clamp(p.sprintSpeedMean, 4, 8);
        p.strafeWobbleAmp      = clamp(p.strafeWobbleAmp, 0, 0.4);
        p.jumpLeadTimeMs       = clamp(p.jumpLeadTimeMs, 0, 300);
        p.pauseChancePerSecond = clamp(p.pauseChancePerSecond, 0, 0.2);
        p.arrivalOvershootBlocks = clamp(p.arrivalOvershootBlocks, 0, 0.5);
        return p;
    }

    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    private ProfileValidator() {}
}
