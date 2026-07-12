package com.zenith.client.engine.path.learning;

import java.util.Arrays;

/**
 * Statistical profile of a player's movement style, distilled from recorded
 * {@link MovementSample}s. Each dimension records mean + stddev so we can
 * reproduce natural human variance without "perfection".
 *
 * <p>Profiles are persisted under {@code data/movement_profiles/*.json} so the
 * model builds up over sessions. A {@link DefaultProfile} is always present as
 * fallback so the system works brilliantly even before any learning happens.</p>
 */
public class MovementProfile {

    public String name = "default";

    // ---- Turn metrics ----
    public double turnMeanDegPerSec = 96d;     // average angular speed while turning
    public double turnStdDegPerSec  = 24d;
    public double turnAccelMean     = 190d;    // deg/s²
    public double turnAccelStd      = 45d;
    public double overshootMeanDeg  = 1.6d;
    public double overshootChance   = 0.35d;
    public double microCorrectionDegPerSec = 80d;

    // ---- Movement metrics ----
    public double walkSpeedMean     = 4.3d;    // blocks/s
    public double walkSpeedStd      = 0.25d;
    public double sprintSpeedMean   = 5.6d;
    public double sprintSpeedStd    = 0.3d;
    public double sprintBurstLengthMeanMs = 750d;
    public double sprintBurstLengthStdMs  = 250d;
    public double strafeWobbleAmp   = 0.06d;
    public double strafeWobblePeriodMs = 2200d;
    public double jumpLeadTimeMs    = 85d;    // ms before edge they press jump
    public double jumpLeadTimeStdMs = 30d;
    public double airStrafeBias     = 0.12d;

    // ---- Hesitation/pauses ----
    public double pauseChancePerSecond = 0.02d;
    public double pauseDurationMeanMs = 320d;
    public double pauseDurationStdMs  = 120d;

    // ---- Consistency/arrival ----
    public double arrivalOvershootBlocks = 0.08d;
    public double arrivalSettleMs = 130d;

    /** Profile version (bump when adding fields) */
    public int schemaVersion = 1;

    public void blend(MovementProfile other, double w) {
        w = Math.max(0d, Math.min(1d, w));
        turnMeanDegPerSec = lerp(turnMeanDegPerSec, other.turnMeanDegPerSec, w);
        turnStdDegPerSec = lerp(turnStdDegPerSec, other.turnStdDegPerSec, w);
        turnAccelMean = lerp(turnAccelMean, other.turnAccelMean, w);
        turnAccelStd = lerp(turnAccelStd, other.turnAccelStd, w);
        overshootMeanDeg = lerp(overshootMeanDeg, other.overshootMeanDeg, w);
        overshootChance = lerp(overshootChance, other.overshootChance, w);
        microCorrectionDegPerSec = lerp(microCorrectionDegPerSec, other.microCorrectionDegPerSec, w);
        walkSpeedMean = lerp(walkSpeedMean, other.walkSpeedMean, w);
        walkSpeedStd = lerp(walkSpeedStd, other.walkSpeedStd, w);
        sprintSpeedMean = lerp(sprintSpeedMean, other.sprintSpeedMean, w);
        sprintSpeedStd = lerp(sprintSpeedStd, other.sprintSpeedStd, w);
        sprintBurstLengthMeanMs = lerp(sprintBurstLengthMeanMs, other.sprintBurstLengthMeanMs, w);
        sprintBurstLengthStdMs = lerp(sprintBurstLengthStdMs, other.sprintBurstLengthStdMs, w);
        strafeWobbleAmp = lerp(strafeWobbleAmp, other.strafeWobbleAmp, w);
        strafeWobblePeriodMs = lerp(strafeWobblePeriodMs, other.strafeWobblePeriodMs, w);
        jumpLeadTimeMs = lerp(jumpLeadTimeMs, other.jumpLeadTimeMs, w);
        jumpLeadTimeStdMs = lerp(jumpLeadTimeStdMs, other.jumpLeadTimeStdMs, w);
        airStrafeBias = lerp(airStrafeBias, other.airStrafeBias, w);
        pauseChancePerSecond = lerp(pauseChancePerSecond, other.pauseChancePerSecond, w);
        pauseDurationMeanMs = lerp(pauseDurationMeanMs, other.pauseDurationMeanMs, w);
        pauseDurationStdMs = lerp(pauseDurationStdMs, other.pauseDurationStdMs, w);
        arrivalOvershootBlocks = lerp(arrivalOvershootBlocks, other.arrivalOvershootBlocks, w);
        arrivalSettleMs = lerp(arrivalSettleMs, other.arrivalSettleMs, w);
    }

    private static double lerp(double a, double b, double w) { return a + (b - a) * w; }
}
