package com.zenith.client.engine.path.learning.profile;

import com.zenith.client.engine.path.learning.MovementProfile;

/** Default baseline profile — carefully hand-tuned to match an average good player. */
public final class DefaultProfile {
    public static MovementProfile build() {
        MovementProfile p = new MovementProfile();
        p.name = "default";
        p.turnMeanDegPerSec = 120; p.turnStdDegPerSec = 35;
        p.turnAccelMean = 220;     p.turnAccelStd = 60;
        p.overshootMeanDeg = 1.8;  p.overshootChance = 0.40;
        p.microCorrectionDegPerSec = 95;
        p.walkSpeedMean = 4.32;    p.walkSpeedStd = 0.28;
        p.sprintSpeedMean = 5.62;  p.sprintSpeedStd = 0.32;
        p.sprintBurstLengthMeanMs = 800; p.sprintBurstLengthStdMs = 300;
        p.strafeWobbleAmp = 0.08;  p.strafeWobblePeriodMs = 2400;
        p.jumpLeadTimeMs = 90;     p.jumpLeadTimeStdMs = 35;
        p.airStrafeBias = 0.14;
        p.pauseChancePerSecond = 0.025;
        p.pauseDurationMeanMs = 350; p.pauseDurationStdMs = 140;
        p.arrivalOvershootBlocks = 0.09;
        p.arrivalSettleMs = 145;
        return p;
    }

    private DefaultProfile() {}
}
