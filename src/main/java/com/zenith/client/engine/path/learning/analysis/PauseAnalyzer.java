package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Detects "thinking pauses" — brief stops with no input. */
public final class PauseAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats pauseLen = new StreamingStats();
        long pauseStart = -1;
        for (MovementSample s : samples) {
            boolean stopped = Math.abs(s.forward) < 0.01 && Math.abs(s.strafe) < 0.01;
            if (stopped && pauseStart == -1 && !s.jump) pauseStart = s.t;
            else if (!stopped && pauseStart != -1) {
                long len = s.t - pauseStart;
                if (len > 100 && len < 2500) pauseLen.add(len);
                pauseStart = -1;
            }
        }
        if (pauseLen.count() == 0) pauseLen.add(320);
        double perSec = (double) pauseLen.count() / Math.max(1, (samples.get(samples.size()-1).t - samples.get(0).t) / 1000d);
        return new Stats(perSec, pauseLen.mean(), pauseLen.stddev());
    }
    public record Stats(double perSecond, double meanMs, double stdMs) {}
}
