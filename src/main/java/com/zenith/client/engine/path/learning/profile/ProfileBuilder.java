package com.zenith.client.engine.path.learning.profile;

import com.zenith.client.engine.path.learning.MovementProfile;
import com.zenith.client.engine.path.learning.MovementSample;
import com.zenith.client.engine.path.learning.analysis.*;

import java.util.List;

/** Reduces a batch of recorded samples into a {@link MovementProfile}. */
public final class ProfileBuilder {

    private final SpeedAnalyzer    speed    = new SpeedAnalyzer();
    private final TurnAnalyzer     turn     = new TurnAnalyzer();
    private final JumpAnalyzer     jump     = new JumpAnalyzer();
    private final SprintAnalyzer   sprint   = new SprintAnalyzer();
    private final PauseAnalyzer    pause    = new PauseAnalyzer();
    private final PathDeviationAnalyzer dev = new PathDeviationAnalyzer();
    private final StrafingAnalyzer strafe   = new StrafingAnalyzer();
    private final ArrivalAnalyzer  arrival  = new ArrivalAnalyzer();

    public MovementProfile build(List<MovementSample> samples, String name) {
        MovementProfile p = new MovementProfile();
        p.name = name;
        if (samples == null || samples.isEmpty()) return DefaultProfile.build();

        var sp = speed.analyze(samples);
        p.walkSpeedMean   = sp.walkMean();    p.walkSpeedStd = sp.walkStd();
        p.sprintSpeedMean = sp.sprintMean();  p.sprintSpeedStd = sp.sprintStd();

        var tu = turn.analyze(samples);
        p.turnMeanDegPerSec = tu.meanDegPerSec(); p.turnStdDegPerSec = tu.stdDegPerSec();
        p.overshootMeanDeg = tu.overshootMeanDeg(); p.overshootChance = tu.overshootChance();

        var j = jump.analyze(samples);
        p.jumpLeadTimeMs = j.leadMs(); p.jumpLeadTimeStdMs = j.leadStdMs();
        p.airStrafeBias = j.airStrafe();

        var sp2 = sprint.analyze(samples);
        p.sprintBurstLengthMeanMs = sp2.burstMs(); p.sprintBurstLengthStdMs = sp2.burstStdMs();

        var pa = pause.analyze(samples);
        p.pauseChancePerSecond = pa.perSecond(); p.pauseDurationMeanMs = pa.meanMs();
        p.pauseDurationStdMs = pa.stdMs();

        var deva = dev.analyze(samples);
        p.strafeWobbleAmp = deva.amplitude(); p.strafeWobblePeriodMs = deva.periodMs();

        var ar = arrival.analyze(samples);
        p.arrivalOvershootBlocks = ar.overshootBlocks(); p.arrivalSettleMs = ar.settleMs();

        return p;
    }
}
